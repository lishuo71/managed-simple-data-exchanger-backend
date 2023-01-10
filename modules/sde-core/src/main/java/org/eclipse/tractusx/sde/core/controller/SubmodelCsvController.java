package org.eclipse.tractusx.sde.core.controller;

import static org.eclipse.tractusx.sde.common.constants.CommonConstants.CSV_FILE_EXTENSION;

import org.eclipse.tractusx.sde.core.service.SubmodelCsvService;
import org.eclipse.tractusx.sde.core.utils.CsvUtil;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@Controller
@ControllerAdvice
@RequiredArgsConstructor
public class SubmodelCsvController {

	private final SubmodelCsvService submodelCsvService;

	private final CsvUtil csvUtil;

	@SneakyThrows
	@GetMapping(value = "/submodels/csvfile/{submodelName}")
	public ResponseEntity<Resource> getSubmodelCSV(@PathVariable String submodelName,
			@RequestParam("type") String type) {

		String filename = submodelName + type + CSV_FILE_EXTENSION;
		return csvUtil.generateCSV(filename, submodelCsvService.findSubmodelCsv(submodelName, type));
	}

	@GetMapping(value = "/{submodel}/download/{processId}/csv")
	@PreAuthorize("hasPermission('','provider_download_own_data')")
	public ResponseEntity<Resource> getDownloadFileByProcessId(@PathVariable("processId") String processId,
			@PathVariable("submodel") String submodel) {

		String filename = submodel + "_" + processId + CSV_FILE_EXTENSION;
		return csvUtil.generateCSV(filename, submodelCsvService.findAllSubmodelCsvHistory(submodel, processId));
	}

}
