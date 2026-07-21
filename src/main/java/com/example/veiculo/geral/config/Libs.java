package com.example.veiculo.geral.config;

import com.example.veiculo.geral.config.exception.personal.RegistroNaoEncontradoException;
import com.example.veiculo.geral.config.exception.personal.RegistroNaoEnviadoException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Objects;

public class Libs {

    public static void validaDtOpera(OffsetDateTime dtOpera) {
        if (dtOpera == null) throw new RegistroNaoEnviadoException("Data Operação não informada");
    }

    public static String getNomeFile(String fileName) { return getNomeFile(fileName, "", false); }
    public static String getNomeFile(String fileName, String frase1_ou_frase2, boolean mp3) {
        return fileName.replace(";","-")
                .replace(":","#")
                .replace("/","" )
                .replace("\"","")
                .replace(" ","_")
                .replace("?","" )
                .replace("!","" ) + frase1_ou_frase2 + (mp3 ? ".mp3" : ".webm");
    }

    public static String getNomeStatusReserva(String status) {
        return Objects.equals(status, "A") ? "A Venda" :
               Objects.equals(status, "R") ? "Reservado" :
               Objects.equals(status, "V") ? "Vendido" :
               Objects.equals(status, "C") ? "Cancelado" : "";
    }

    public static String getNomeFileTradutor(String fileName) {
        return fileName.replace("_"," ")
                       .replace("" ,"" );
    }

    public static boolean isNullEmpty(String valor) {
        return (valor == null || valor.isEmpty());
    }

    public static boolean isEqual(String valor, String dadoCamporacao) {
        if (valor == null || valor.isEmpty()) return false;
        return (valor.equals(dadoCamporacao));
    }

    public static void isIdNull(Long id) {
        if (id == null) throw new RegistroNaoEncontradoException("O ID não foi informado");
    }

    public static void isIdNullOrEmpty(String id) {
        if (id == null || id.isEmpty()) throw new RegistroNaoEncontradoException("O ID não foi informado");
    }

    public static String isNullGetEmpty(Long id) {
        if (id == null) return "";
        return id.toString();
    }

    public static boolean isNotEqual(String valor, String dadoCamporacao) {
        return !isEqual(valor, dadoCamporacao);
    }

    public static boolean existChar(String valor, String valorPesq) {return valor.contains("+"); }

    public static boolean isNull(Long valor) {
        return (valor == null);
    }

    public enum TpOpe {
        INCLUSAO,
        ALTERACAO,
        ALTERACAO_ALL,
        EXCLUSAO,
        CONFIRMACAO,
        EXIT,
        OUTROS,
        SoID
    }

    public static boolean isNull(LocalDate valor) { return (valor == null); }

    public static boolean isNull(LocalDateTime valor) { return (valor == null); }

    public static boolean isNullOrInteger(String valor) {
        if (valor == null || valor.trim().isEmpty()) return true; // É nulo ou vazio
        var ret = valor.matches("\\d+"); // Retorna true se contiver qualquer coisa que não seja dígito
        return ret; // Retorna true se contiver qualquer coisa que não seja dígito
    }
}
