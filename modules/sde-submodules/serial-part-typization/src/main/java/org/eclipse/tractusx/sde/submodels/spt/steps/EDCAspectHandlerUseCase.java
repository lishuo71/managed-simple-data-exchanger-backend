/********************************************************************************
 * Copyright (c) 2022 Critical TechWorks GmbH
 * Copyright (c) 2022 BMW GmbH
 * Copyright (c) 2022 T-Systems International GmbH
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/

package org.eclipse.tractusx.sde.submodels.spt.steps;

import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tractusx.sde.common.enums.UsagePolicyEnum;
import org.eclipse.tractusx.sde.common.exception.CsvHandlerUseCaseException;
import org.eclipse.tractusx.sde.common.submodel.executor.Step;
import org.eclipse.tractusx.sde.edc.entities.request.asset.AssetEntryRequest;
import org.eclipse.tractusx.sde.edc.entities.request.asset.AssetEntryRequestFactory;
import org.eclipse.tractusx.sde.edc.entities.request.contractdefinition.ContractDefinitionRequest;
import org.eclipse.tractusx.sde.edc.entities.request.contractdefinition.ContractDefinitionRequestFactory;
import org.eclipse.tractusx.sde.edc.entities.request.policies.ConstraintRequest;
import org.eclipse.tractusx.sde.edc.entities.request.policies.PolicyConstraintBuilderService;
import org.eclipse.tractusx.sde.edc.entities.request.policies.PolicyDefinitionRequest;
import org.eclipse.tractusx.sde.edc.entities.request.policies.PolicyRequestFactory;
import org.eclipse.tractusx.sde.edc.facilitator.DeleteEDCFacilitator;
import org.eclipse.tractusx.sde.edc.gateways.external.EDCGateway;
import org.eclipse.tractusx.sde.submodels.spt.entity.AspectEntity;
import org.eclipse.tractusx.sde.submodels.spt.mapper.AspectMapper;
import org.eclipse.tractusx.sde.submodels.spt.model.Aspect;
import org.eclipse.tractusx.sde.submodels.spt.service.AspectService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import lombok.SneakyThrows;

@Service
public class EDCAspectHandlerUseCase extends Step {

	private static final String ASSET_PROP_NAME_ASPECT = "Serialized Part - Submodel SerialPartTypization";
    
	private final AssetEntryRequestFactory assetFactory;
	private final EDCGateway edcGateway;
	private final PolicyRequestFactory policyFactory;
	private final ContractDefinitionRequestFactory contractFactory;
	private final PolicyConstraintBuilderService policyConstraintBuilderService;
	private final DeleteEDCFacilitator deleteEDCFacilitator;
	private final AspectService aspectService;
	private final AspectMapper aspectMapper;

	public EDCAspectHandlerUseCase(EDCGateway edcGateway, AssetEntryRequestFactory assetFactory,
			PolicyRequestFactory policyFactory, ContractDefinitionRequestFactory contractFactory,
			PolicyConstraintBuilderService policyConstraintBuilderService,DeleteEDCFacilitator deleteEDCFacilitator,AspectService aspectService,AspectMapper aspectMapper) {
		this.assetFactory = assetFactory;
		this.edcGateway = edcGateway;
		this.policyFactory = policyFactory;
		this.contractFactory = contractFactory;
		this.policyConstraintBuilderService = policyConstraintBuilderService;
		this.deleteEDCFacilitator=deleteEDCFacilitator;
		this.aspectService=aspectService;
		this.aspectMapper=aspectMapper;
	}

	@SneakyThrows
	public Aspect run(String submodel, Aspect input, String processId) {
		HashMap<String, String> extensibleProperties = new HashMap<>();
		String shellId = input.getShellId();
		String subModelId = input.getSubModelId();

		try {

			AssetEntryRequest assetEntryRequest = assetFactory.getAssetRequest(submodel, ASSET_PROP_NAME_ASPECT,
					shellId, subModelId, input.getUuid());
			if (!edcGateway.assetExistsLookup(assetEntryRequest.getAsset().getProperties().get("asset:prop:id"))) {

				edcProcessingforAspect(assetEntryRequest, input);

			} else {

				// Delete code Goes here
				deleteEDCFirstForUpdate(submodel, input, processId);
				/// Add new COde for asset, contract defination, usage policy, access policy.
				edcProcessingforAspect(assetEntryRequest, input);
			}

			return input;
		} catch (Exception e) {
			throw new CsvHandlerUseCaseException(input.getRowNumber(), "EDC: " + e.getMessage());
		}
	}

	@SneakyThrows
	private void deleteEDCFirstForUpdate(String submodel, Aspect input, String processId) {
		AspectEntity aspectEntity = aspectMapper.mapforEntity(aspectService.readCreatedTwinsDetails(input.getUuid()));
		deleteEDCFacilitator.deleteContractDefination(aspectEntity.getContractDefinationId());

		deleteEDCFacilitator.deleteAccessPolicy(aspectEntity.getAccessPolicyId());

		deleteEDCFacilitator.deleteUsagePolicy(aspectEntity.getUsagePolicyId());

		deleteEDCFacilitator.deleteAssets(aspectEntity.getAssetId());
	}
	
	@SneakyThrows
	private void edcProcessingforAspect(AssetEntryRequest assetEntryRequest, Aspect input) {
		HashMap<String, String> extensibleProperties = new HashMap<>();

		String shellId = input.getShellId();
		String subModelId = input.getSubModelId();
		edcGateway.createAsset(assetEntryRequest);

		List<ConstraintRequest> usageConstraints = policyConstraintBuilderService
				.getUsagePolicyConstraints(input.getUsagePolicies());
		List<ConstraintRequest> accessConstraints = policyConstraintBuilderService
				.getAccessConstraints(input.getBpnNumbers());

		String customValue = getCustomValue(input);
		if (StringUtils.isNotBlank(customValue)) {
			extensibleProperties.put(UsagePolicyEnum.CUSTOM.name(), customValue);
		}

		PolicyDefinitionRequest accessPolicyDefinitionRequest = policyFactory.getPolicy(shellId, subModelId,
				accessConstraints, new HashMap<>());
		PolicyDefinitionRequest usagePolicyDefinitionRequest = policyFactory.getPolicy(shellId, subModelId,
				usageConstraints, extensibleProperties);

		edcGateway.createPolicyDefinition(accessPolicyDefinitionRequest);

		edcGateway.createPolicyDefinition(usagePolicyDefinitionRequest);

		ContractDefinitionRequest contractDefinitionRequest = contractFactory.getContractDefinitionRequest(
				assetEntryRequest.getAsset().getProperties().get("asset:prop:id"),
				accessPolicyDefinitionRequest.getId(), usagePolicyDefinitionRequest.getId());

		edcGateway.createContractDefinition(contractDefinitionRequest);

		// EDC transaction information for DB
		input.setAssetId(assetEntryRequest.getAsset().getProperties().get("asset:prop:id"));
		input.setAccessPolicyId(accessPolicyDefinitionRequest.getId());
		input.setUsagePolicyId(usagePolicyDefinitionRequest.getId());
		input.setContractDefinationId(contractDefinitionRequest.getId());
	}


	private String getCustomValue(Aspect input) {
		if (!CollectionUtils.isEmpty(input.getUsagePolicies())) {
			return input.getUsagePolicies().stream().filter(policy -> policy.getType().equals(UsagePolicyEnum.CUSTOM))
					.map(value -> value.getValue()).findFirst().orElse(null);
		}
		return null;
	}

}