package com.example.veiculo.repository;

import com.example.veiculo.dto.ReervaVendaVeiculo.VeiculoMarcaDtoSaida;
import com.example.veiculo.dto.ReervaVendaVeiculo.VeiculoMarcaModeloDtoSaida;
import com.example.veiculo.dto.ReervaVendaVeiculo.VeiculoMarcaModeloVersaoDtoSaida;
import com.example.veiculo.dto.ReervaVendaVeiculo.VeiculoMarcaModeloVersaoQtdeEstoqueDtoSaida;
import com.example.veiculo.model.ReservaVendaVeiculo;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservaVendaVeiculoRepository extends JpaRepository<ReservaVendaVeiculo, Long> {
    List<ReservaVendaVeiculo> findByStatus(String status, Sort sort);
    boolean existsById(Long id);
    boolean existsByVeiculoIdAndStatus(Long id, String status);
    boolean existsByVeiculoIdAndStatusAndIdNot(Long veiculoId, String status, Long reservaId);
    List<ReservaVendaVeiculo> findByStatusOrderByValorAsc(String status, Sort sort);
    List<ReservaVendaVeiculo> findByStatusAndRetiradoOrderByValorAsc(String status, String retirado, Sort sort);
    boolean existsByVeiculoId(Long id);
    boolean existsByClienteId(Long id);

    @Query(nativeQuery = true,
            value = "SELECT a.nome || ' ' || m.nome || ' ' || s.nome as descricao, SUM(1) AS qtdeEstoque" +
                     " FROM public.veiculo v left join public.versao  s on v.id_versao  = s.id" +
                                           " left join public.modelo  m on s.id_modelo  = m.id" +
                                           " left join public.marca   a on m.id_marca   = a.id" +
                    " WHERE v.status = 'A'" +
                    " GROUP BY a.nome, m.nome, s.nome" +
                    " ORDER BY a.nome, m.nome, s.nome")
    List<VeiculoMarcaModeloVersaoQtdeEstoqueDtoSaida> findVeiculoMarcaModeloVersaoQtdeEstoque();

    @Query(nativeQuery = true,
            value = "SELECT a.nome || ' ' || m.nome || ' ' || s.nome as descricao," +
                          " SUM(CASE WHEN v.status = 'A' THEN 1 ELSE 0 END) AS qtdeDisponivel," +
                          " SUM(CASE WHEN v.status = 'R' THEN 1 ELSE 0 END) AS qtdeReservada," +
                          " SUM(CASE WHEN v.status = 'V' THEN 1 ELSE 0 END) AS qtdeVendida," +
                          " count(*) qtdeTotal" +
                    " FROM public.veiculo v left join public.versao  s on v.id_versao  = s.id" +
                                          " left join public.modelo  m on s.id_modelo  = m.id" +
                                          " left join public.marca   a on m.id_marca   = a.id" +
                    " GROUP BY a.nome, m.nome, s.nome" +
                    " ORDER BY a.nome, m.nome, s.nome")
    List<VeiculoMarcaModeloVersaoDtoSaida> findVeiculoMarcaModeloVersao();

    @Query(nativeQuery = true,
            value = "SELECT a.nome || ' ' || m.nome as descricao," +
                          " SUM(CASE WHEN v.status = 'A' THEN 1 ELSE 0 END) AS qtdeDisponivel," +
                          " SUM(CASE WHEN v.status = 'R' THEN 1 ELSE 0 END) AS qtdeReservada," +
                          " SUM(CASE WHEN v.status = 'V' THEN 1 ELSE 0 END) AS qtdeVendida," +
                          " count(*) qtdeTotal" +
                    " FROM public.veiculo v left join public.versao  s on v.id_versao  = s.id" +
                                          " left join public.modelo  m on s.id_modelo  = m.id" +
                                          " left join public.marca   a on m.id_marca   = a.id" +
                    " GROUP BY a.nome, m.nome" +
                    " ORDER BY a.nome, m.nome")
    List<VeiculoMarcaModeloDtoSaida> findVeiculoMarcaModelo();

    @Query(nativeQuery = true,
            value = "SELECT a.nome as marca," +
                          " SUM(CASE WHEN v.status = 'A' THEN 1 ELSE 0 END) AS qtdeDisponivel," +
                          " SUM(CASE WHEN v.status = 'R' THEN 1 ELSE 0 END) AS qtdeReservada," +
                          " SUM(CASE WHEN v.status = 'V' THEN 1 ELSE 0 END) AS qtdeVendida," +
                          " count(*) qtdeTotal" +
                     " FROM public.veiculo v left join public.versao  s on v.id_versao  = s.id" +
                                           " left join public.modelo  m on s.id_modelo  = m.id" +
                                           " left join public.marca   a on m.id_marca   = a.id" +
                    " GROUP BY a.nome" +
                    " ORDER BY a.nome")

    List<VeiculoMarcaDtoSaida> findVeiculoMarca();

    //@Param("permissaoName") String permissaoName, @Param("operacaoId") String registroId
    // WHERE r.status = 'C'

    //boolean existsByNome(String nome);
   //boolean existsByNomeAndIdNot(String nome, Long id);
}
