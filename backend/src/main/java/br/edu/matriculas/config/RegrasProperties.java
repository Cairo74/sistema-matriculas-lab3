package br.edu.matriculas.config;

import java.math.BigDecimal;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/** Parâmetros das regras de negócio, ajustáveis em {@code application.yml}. */
@ConfigurationProperties("matriculas.regras")
public record RegrasProperties(
        @DefaultValue("60") int vagasPorDisciplina,
        @DefaultValue("3") int minimoAlunos,
        @DefaultValue("4") int maxObrigatorias,
        @DefaultValue("2") int maxOptativas,
        @DefaultValue("389.90") BigDecimal valorCredito) {
}
