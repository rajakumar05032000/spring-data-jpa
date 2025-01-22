/*
 * Copyright 2013-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.jpa.repository.query;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.repository.query.ParameterBindingParser.Metadata;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * Encapsulation of a JPA query String. Offers access to parameters as bindings. The internal query String is cleaned
 * from decorated parameters like {@literal %:lastname%} and the matching bindings take care of applying the decorations
 * in the {@link ParameterBinding#prepare(Object)} method. Note that this class also handles replacing SpEL expressions
 * with synthetic bind parameters.
 *
 * @author Oliver Gierke
 * @author Thomas Darimont
 * @author Oliver Wehrens
 * @author Mark Paluch
 * @author Jens Schauder
 * @author Diego Krupitza
 * @author Greg Turnquist
 * @author Yuriy Tsarkov
 */
class StringQuery implements DeclaredQuery {

	private final String query;
	private final List<ParameterBinding> bindings;
	private final boolean containsPageableInSpel;
	private final boolean usesJdbcStyleParameters;
	private final boolean isNative;
	private final QueryEnhancer queryEnhancer;
	private final boolean hasNamedParameters;

	/**
	 * Creates a new {@link StringQuery} from the given JPQL query.
	 *
	 * @param query must not be {@literal null} or empty.
	 */
	public StringQuery(String query, boolean isNative) {

		Assert.hasText(query, "Query must not be null or empty");

		this.isNative = isNative;
		this.bindings = new ArrayList<>();
		this.containsPageableInSpel = query.contains("#pageable");

		Metadata queryMeta = new Metadata();
		this.query = ParameterBindingParser.INSTANCE.parseParameterBindingsOfQueryIntoBindingsAndReturnCleanedQuery(query,
				this.bindings, queryMeta);

		this.usesJdbcStyleParameters = queryMeta.usesJdbcStyleParameters();
		this.queryEnhancer = QueryEnhancerFactory.forQuery(this);

		boolean hasNamedParameters = false;
		for (ParameterBinding parameterBinding : getParameterBindings()) {
			if (parameterBinding.getIdentifier().hasName() && parameterBinding.getOrigin().isMethodArgument()) {
				hasNamedParameters = true;
				break;
			}
		}

		this.hasNamedParameters = hasNamedParameters;
	}

	/**
	 * Returns whether we have found some like bindings.
	 */
	boolean hasParameterBindings() {
		return !bindings.isEmpty();
	}

	String getProjection() {
		return this.queryEnhancer.getProjection();
	}

	@Override
	public List<ParameterBinding> getParameterBindings() {
		return bindings;
	}

	@Override
	public DeclaredQuery deriveCountQuery(@Nullable String countQueryProjection) {

		StringQuery stringQuery = new StringQuery(this.queryEnhancer.createCountQueryFor(countQueryProjection), //
				this.isNative);

		if (this.hasParameterBindings() && !this.getParameterBindings().equals(stringQuery.getParameterBindings())) {
			stringQuery.getParameterBindings().clear();
			stringQuery.getParameterBindings().addAll(this.bindings);
		}

		return stringQuery;
	}

	@Override
	public boolean usesJdbcStyleParameters() {
		return usesJdbcStyleParameters;
	}

	@Override
	public String getQueryString() {
		return query;
	}

	@Override
	@Nullable
	public String getAlias() {
		return queryEnhancer.detectAlias();
	}

	@Override
	public boolean hasConstructorExpression() {
		return queryEnhancer.hasConstructorExpression();
	}

	@Override
	public boolean isDefaultProjection() {
		return getProjection().equalsIgnoreCase(getAlias());
	}

	@Override
	public boolean hasNamedParameter() {
		return hasNamedParameters;
	}

	@Override
	public boolean usesPaging() {
		return containsPageableInSpel;
	}

	@Override
	public boolean isNativeQuery() {
		return isNative;
	}
}
