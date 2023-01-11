/********************************************************************************
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

package org.eclipse.tractusx.sde.edc.api;

import java.net.URI;
import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(value = "EDCFeignClientApi", url = "placeholder")
public interface EDCFeignClientApi {

	@DeleteMapping(path = "/contractdefinitions/{id}")
	ResponseEntity<Object> deleteContractDefinition(URI url, @PathVariable("id") String contractdefinitionsId,
			@RequestHeader Map<String, String> requestHeader);

	@DeleteMapping(path = "/policydefinitions/{id}")
	ResponseEntity<Object> deletePolicyDefinitions(URI url, @PathVariable("id") String policydefinitionsId,
			@RequestHeader Map<String, String> requestHeader);

	@DeleteMapping(path = "/assets/{id}")
	ResponseEntity<Object> deleteAssets(URI url, @PathVariable("id") String assetsId,
			@RequestHeader Map<String, String> requestHeader);

}
