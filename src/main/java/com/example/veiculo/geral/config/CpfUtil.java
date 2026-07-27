package com.example.veiculo.geral.config;

/**
 * Utilitário para validação e mascaramento de CPF (LGPD).
 */
public final class CpfUtil {

    private CpfUtil() {
    }

    public static String somenteDigitos(String cpf) {
        return cpf == null ? null : cpf.replaceAll("\\D", "");
    }

    /**
     * Valida CPF pelos dígitos verificadores.
     */
    public static boolean isValido(String cpf) {
        String d = somenteDigitos(cpf);
        if (d == null || d.length() != 11) return false;
        if (d.chars().distinct().count() == 1) return false; // todos os dígitos iguais

        try {
            int soma = 0;
            for (int i = 0; i < 9; i++) soma += (d.charAt(i) - '0') * (10 - i);
            int dv1 = 11 - (soma % 11);
            if (dv1 >= 10) dv1 = 0;
            if (dv1 != d.charAt(9) - '0') return false;

            soma = 0;
            for (int i = 0; i < 10; i++) soma += (d.charAt(i) - '0') * (11 - i);
            int dv2 = 11 - (soma % 11);
            if (dv2 >= 10) dv2 = 0;
            return dv2 == d.charAt(10) - '0';
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Mascara o CPF ocultando os 3 primeiros e os 2 últimos dígitos: ***.456.789-**
     */
    public static String mascarar(String cpf) {
        String d = somenteDigitos(cpf);
        if (d == null || d.length() != 11) return "***.***.***-**";
        return "***." + d.substring(3, 6) + "." + d.substring(6, 9) + "-**";
    }
}
