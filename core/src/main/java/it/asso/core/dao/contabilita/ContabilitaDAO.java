package it.asso.core.dao.contabilita;

import it.asso.core.dao.common.BaseRowMapper;
import it.asso.core.model.contabilita.Pagamento;
import it.asso.core.model.contabilita.PrevisioneSpesa;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;



@Repository
public class ContabilitaDAO {

	private static Logger logger = LoggerFactory.getLogger(ContabilitaDAO.class);
    
    private final JdbcTemplate jdbcTemplate;

    public ContabilitaDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

	
	/**
	 * @param 
	 * @return String
	 */
	@Transactional()
	public List<PrevisioneSpesa> getPrevisioneSpesa() {
		
		String queryStr = "Select id_evento, id_animale, id_tipo_evento, id_contatto, cod_animale, nome, evento, contatto, dal, giorni, ct_gg, costo, dt_pagamento, pagato, da_pagare " + 
				" FROM v_ct_previsioni";
		try{
			return jdbcTemplate.query(queryStr, new PrevisioneSpesaRowMapper());

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	/**
	 * @param 
	 * @return String
	 */
	@Transactional()
	public List<PrevisioneSpesa> getPrevisioneSpesa(String idContatto) {
		
		String queryStr = "Select id_evento, id_animale, id_tipo_evento, id_contatto, cod_animale, nome, evento, contatto, dal, giorni, ct_gg, costo, dt_pagamento, pagato, da_pagare " + 
				" FROM v_ct_previsioni where id_contatto = ?";
		try{
			return jdbcTemplate.query(queryStr, new PrevisioneSpesaRowMapper(), new Object[] { idContatto });

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	
	
	
	private static class PrevisioneSpesaRowMapper extends BaseRowMapper<PrevisioneSpesa> {
		public PrevisioneSpesaRowMapper() {}		
		@Override
		public PrevisioneSpesa mapRowImpl(ResultSet rs, int i) throws SQLException {
		
			PrevisioneSpesa o = new PrevisioneSpesa();
			o.setCod_animale(rs.getString("cod_animale"));
			o.setContatto(rs.getString("contatto"));
			o.setCt_gg(rs.getString("ct_gg"));
			o.setDa_pagare(rs.getString("da_pagare"));
			o.setDal(rs.getString("dal"));
			o.setEvento(rs.getString("evento"));
			o.setGiorni(rs.getString("giorni"));
			o.setId_animale(rs.getString("id_animale"));
			o.setId_contatto(rs.getString("id_contatto"));
			o.setId_evento(rs.getString("id_evento"));
			o.setId_tipo_evento(rs.getString("id_tipo_evento"));
			o.setNome(rs.getString("nome"));
			o.setCosto(rs.getString("costo"));
			o.setPagato(rs.getString("pagato"));
			o.setDt_pagamento(rs.getString("dt_pagamento"));
			return o;
		}
	}
	
	/***********************************************************************************************************/
	
	/**
	 * @param 
	 * @return String
	 */
	@Transactional()
	public List<Pagamento> getPagamentiByEvento(String idEvento) {
		
		String queryStr = "SELECT id_pagamento, id_evento, dt_pagamento, importo, note FROM ct_pagamenti where id_evento = ?";
		try{
			return jdbcTemplate.query(queryStr, new PagamentoRowMapper(), new Object[] { idEvento });

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	/**
	 * @param pagamento
	 * @return
	 */
	
	@Transactional()
	public String saveOrUpdatePagamento(Pagamento pagamento) {
		String idpagamento = "";
		
		if (pagamento.getId_pagamento()  == null) {
			idpagamento = savePagamento(pagamento);
		} else {
			idpagamento = updatePagamento(pagamento);
		}
		return idpagamento;
	}

	/**
	 * @param pagamento
	 * @return
	 */
	
	@Transactional()
	public String savePagamento(Pagamento pagamento) {

		final String query = "INSERT INTO ct_pagamenti " + 
				"(id_evento, " + 
				"dt_pagamento, " + 
				"importo, note) " + 
				" VALUES " + 
				"(:id_evento , " + 
				"str_to_date(nullif(:dt_pagamento,''), '%d/%m/%Y') , " + 
				":importo, :note )";

		KeyHolder keyHolder = new GeneratedKeyHolder();

		SqlParameterSource parameters = new BeanPropertySqlParameterSource(pagamento);
        assert jdbcTemplate.getDataSource() != null;
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters, keyHolder, new String[] { "id_pagamento" });

		return String.valueOf(keyHolder.getKey());
	}

	/**
	 * @param pagamento
	 * @return
	 */
	
	@Transactional()
	public String updatePagamento(Pagamento pagamento) {

		final String query = "UPDATE ct_pagamenti " + 
				" SET " + 
				" id_evento  =  :id_evento ," + 
				" importo  =  :importo ," + 
				" dt_pagamento  =  str_to_date(nullif(:dt_pagamento,''), '%d/%m/%Y'), note = :note " + 
				"WHERE  id_pagamento  = :id_pagamento";

		SqlParameterSource parameters = new BeanPropertySqlParameterSource(pagamento);
        assert jdbcTemplate.getDataSource() != null;
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters);

		return pagamento.getId_pagamento();
	}
	
	/**
	 * @param pagamento
	 * @return
	 */
	
	@Transactional()
	public String deletePagamento(Pagamento pagamento) {
		//TODO prima di eliminare verificare non ci siano costi per pensione e stallo
		final String query = "DELETE FROM ct_pagamenti WHERE id_pagamento = :id_pagamento";

		SqlParameterSource parameters = new BeanPropertySqlParameterSource(pagamento);
        assert jdbcTemplate.getDataSource() != null;
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters);

		return pagamento.getId_evento();
	}
	
	
	private static class PagamentoRowMapper extends BaseRowMapper<Pagamento> {
		public PagamentoRowMapper() {}		
		@Override
		public Pagamento mapRowImpl(ResultSet rs, int i) throws SQLException {
		
			Pagamento o = new Pagamento();
			o.setImporto(rs.getString("importo"));
			o.setId_pagamento(rs.getString("id_pagamento"));
			o.setId_evento(rs.getString("id_evento"));
			o.setDt_pagamento(rs.getString("dt_pagamento"));
			o.setNote(rs.getString("note"));
			return o;
		}
	}
	
	
}
