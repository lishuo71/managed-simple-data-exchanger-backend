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

package com.catenax.dft.configuration;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.GenericFilterBean;

@Configuration
public class ApiHeaderAuthFilter extends GenericFilterBean {

	@Value("${dft.apiKeyHeader}")
	private String apiKeyHeader;

	@Value("${dft.apiKey}")
	private String apiKeyValue;

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
			throws IOException, ServletException {

		var request = (HttpServletRequest) servletRequest;
		var response = (HttpServletResponse) servletResponse;

		String authHeaderValue = request.getHeader(apiKeyHeader);
		String url = request.getRequestURI();

		if (url.contains("/public") && !apiKeyValue.equals(authHeaderValue)) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		} else {
			filterChain.doFilter(servletRequest, servletResponse);
		}
	}
}