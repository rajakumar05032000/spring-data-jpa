/*
 * Copyright 2024 the original author or authors.
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
package org.springframework.data.jpa.repository.aot.generated;

import jakarta.persistence.EntityManager;

import java.lang.reflect.Parameter;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.aot.generate.AotRepositoryConstructorBuilder;
import org.springframework.data.repository.aot.generate.AotRepositoryMethodBuilder;
import org.springframework.data.repository.aot.generate.AotRepositoryMethodGenerationContext;
import org.springframework.data.repository.aot.generate.RepositoryContributor;
import org.springframework.data.repository.config.AotRepositoryContext;
import org.springframework.javapoet.TypeName;
import org.springframework.util.StringUtils;

/**
 * @author Christoph Strobl
 */
public class JpaRepsoitoryContributor extends RepositoryContributor {

	public JpaRepsoitoryContributor(AotRepositoryContext repositoryContext) {
		super(repositoryContext);
	}

	@Override
	protected void customizeConstructor(AotRepositoryConstructorBuilder constructorBuilder) {
		constructorBuilder.addParameter("entityManager", TypeName.get(EntityManager.class));
	}

	@Override
	protected AotRepositoryMethodBuilder contributeRepositoryMethod(
		AotRepositoryMethodGenerationContext generationContext) {

		{
			Query queryAnnotation = AnnotatedElementUtils.findMergedAnnotation(generationContext.getMethod(), Query.class);
			if (queryAnnotation != null) {
				if (StringUtils.hasText(queryAnnotation.value())
					&& Pattern.compile("[\\?:][#$]\\{.*\\}").matcher(queryAnnotation.value()).find()) {
					return null;
				}
			}
		}

		return new AotRepositoryMethodBuilder(generationContext).customize((context, body) -> {

			Query query = AnnotatedElementUtils.findMergedAnnotation(context.getMethod(), Query.class);
			if (query != null) {

				body.addCode(context.codeBlocks().logDebug("invoking [%s]".formatted(context.getMethod().getName())));

				body.addStatement("$T query = this.$L.createQuery($S)", jakarta.persistence.Query.class,
						context.fieldNameOf(EntityManager.class), query.value());
				int i = 1;
				for (Parameter parameter : context.getMethod().getParameters()) {
					body.addStatement("query.setParameter(" + i + ", " + parameter.getName() + ")");
					i++;
				}
				if (!context.returnsVoid()) {
					body.addStatement("return ($T) query.getResultList()", context.getReturnType());
				}
			} else {

			}
		});
	}
}
