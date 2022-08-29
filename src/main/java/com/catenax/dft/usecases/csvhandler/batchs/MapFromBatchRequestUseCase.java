/*
 * Copyright 2022 CatenaX
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.catenax.dft.usecases.csvhandler.batchs;

import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import org.springframework.stereotype.Service;

import com.catenax.dft.entities.batch.BatchRequest;
import com.catenax.dft.entities.usecases.Batch;
import com.catenax.dft.mapper.BatchMapper;
import com.catenax.dft.usecases.csvhandler.AbstractCsvHandlerUseCase;
import com.catenax.dft.usecases.csvhandler.exceptions.CsvHandlerUseCaseException;

import lombok.SneakyThrows;

@Service
public class MapFromBatchRequestUseCase extends AbstractCsvHandlerUseCase<BatchRequest, Batch> {
    private final BatchMapper batchMapper;

    public MapFromBatchRequestUseCase(GenerateBatchUuIdCsvHandlerUseCase nextUseCase, BatchMapper mapper) {
        super(nextUseCase);
        this.batchMapper=mapper;
    }

    @SneakyThrows
    @Override
    protected Batch executeUseCase(BatchRequest input, String processId) {
        Batch batch = batchMapper.mapFrom(input);
        List<String> errorMessages = validateAsset(batch);
        if (!errorMessages.isEmpty()) {
            throw new CsvHandlerUseCaseException(input.getRowNumber(), errorMessages.toString());
        }

        return batch;
    }

    private List<String> validateAsset(Batch asset) {
        Validator validator = Validation.buildDefaultValidatorFactory()
                .getValidator();
        Set<ConstraintViolation<Batch>> violations = validator.validate(asset);

        return violations.stream()
                .map(ConstraintViolation::getMessage)
                .sorted()
                .toList();
    }
}