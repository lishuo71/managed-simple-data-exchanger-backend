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

package com.catenax.dft.usecases.csvhandler;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;

import com.catenax.dft.entities.database.FailureLogEntity;
import com.catenax.dft.usecases.logs.FailureLogsUseCase;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractCsvHandlerUseCase<I, O> implements CsvHandlerUseCase<I> {

    protected CsvHandlerUseCase<O> nextUseCase;
    @Autowired
    private FailureLogsUseCase failureLogsUseCase;

    protected AbstractCsvHandlerUseCase(CsvHandlerUseCase<O> nextUseCase) {
        this.nextUseCase = nextUseCase;
    }

    protected abstract O executeUseCase(I input, String processId);

    @Override
    public void run(I input, String processId) {

        try {
            O result = executeUseCase(input, processId);

            if (nextUseCase != null) {
                nextUseCase.run(result, processId);
            }

        } catch (Exception e) {
            FailureLogEntity entity = FailureLogEntity.builder()
                    .uuid(UUID.randomUUID().toString())
                    .processId(processId)
                    .log(e.getMessage())
                    .dateTime(LocalDateTime.now())
                    .build();
            failureLogsUseCase.saveLog(entity);
            logDebug(String.valueOf(e));
        }
    }

    protected void logDebug(String message) {
        log.debug(String.format("[%s] %s", this.getClass().getSimpleName(), message));
    }
}
