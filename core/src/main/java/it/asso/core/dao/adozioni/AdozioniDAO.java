package it.asso.core.dao.adozioni;

import it.asso.core.common.Def;
import it.asso.core.dao.common.BaseRowMapper;
import it.asso.core.model.adozioni.*;
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
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

@Repository
public class AdozioniDAO {
	
	private static Logger logger = LoggerFactory.getLogger(AdozioniDAO.class);

    private final JdbcTemplate jdbcTemplate;

    public AdozioniDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
	
	
	/*----------------------------------------------- ADOTTANTI ------------------------------------------------*/
	/**
	 * @param search
	 * @return List<Adottante>
	 */
	
	@Transactional()
	public List<Adottante> getAdottanti(String search) {
		if(search == null || "".equals(search)) {
			search = Def.STR_PERCENTAGE;
		}else {
			search = Def.STR_PERCENTAGE + search + Def.STR_PERCENTAGE;
		}
		String queryStr = "Select " + 
				"    c.id_contatto, " + 
				"    c.nome, " + 
				"    c.cognome, " + 
				"    c.blacklist, " + 
				"    adn.id_adottante, " + 
				"    adn.wallet, " + 
				"    DATE_FORMAT(nullif(adn.dt_inserimento,''), '%d/%m/%Y') dt_inserimento, " + 
				"    adn.account, " + 
				"    adn.attivo, " + 
				"    IFNULL((Select Sum(md_adozioni.quota) From md_adozioni Where md_adozioni.id_adottante = adn.id_adottante), 0) wallet_occupato, " + 
				"    IFNULL(round((Select Sum(md_adozioni.quota) From md_adozioni  Where md_adozioni.id_adottante = adn.id_adottante) * 100 / adn.wallet,2),0) perc_occupato, " + 
				"    round(adn.wallet - IFNULL((Select Sum(md_adozioni.quota) From md_adozioni Where md_adozioni.id_adottante = adn.id_adottante),0),2) wallet_disponibile, " + 
				"    round((adn.wallet - IFNULL((Select Sum(md_adozioni.quota) From md_adozioni Where md_adozioni.id_adottante = adn.id_adottante),0)) * 100 / adn.wallet,2) perc_disponibile " + 
				"From " + 
				"    md_adottanti adn Inner Join " + 
				"    an_contatti c On adn.id_contatto = c.id_contatto " + 
				"Where " + 
				"    c.blacklist = 0 and concat(upper(c.nome), upper(c.cognome)) like upper(?) order by c.cognome";
		try{
			return jdbcTemplate.query(queryStr, new AdottanteRowMapper(), new Object[] { search });
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	/**
	 * @param idAdottabile
	 * @return List<Adottante>
	 */
	
	@Transactional()
	public List<Adottante> getAdottantiByIdAdottabile(String idAdottabile) {
		String queryStr = "Select " + 
				"    c.id_contatto, " + 
				"    c.nome, " + 
				"    c.cognome, " + 
				"    c.blacklist, " + 
				"    adn.id_adottante, " + 
				"    adn.wallet, " + 
				"    DATE_FORMAT(nullif(adn.dt_inserimento,''), '%d/%m/%Y') dt_inserimento, " + 
				"    adn.account, " + 
				"    adn.attivo, " + 
				"    IFNULL((Select Sum(md_adozioni.quota) From md_adozioni Where md_adozioni.id_adottante = adn.id_adottante), 0) wallet_occupato, " + 
				"    IFNULL(round((Select Sum(md_adozioni.quota) From md_adozioni  Where md_adozioni.id_adottante = adn.id_adottante) * 100 / adn.wallet,2),0) perc_occupato, " + 
				"    round(adn.wallet - IFNULL((Select Sum(md_adozioni.quota) From md_adozioni Where md_adozioni.id_adottante = adn.id_adottante),0),2) wallet_disponibile, " + 
				"    round((adn.wallet - IFNULL((Select Sum(md_adozioni.quota) From md_adozioni Where md_adozioni.id_adottante = adn.id_adottante),0)) * 100 / adn.wallet,2) perc_disponibile " + 
				" From  md_adottanti adn Inner Join " +
			    " an_contatti c On adn.id_contatto = c.id_contatto Inner Join " +
			    " md_adozioni adz On adz.id_adottante = adn.id_adottante " +
			    " where adz.id_adottabile = ? " + 
				" order by c.cognome";
		try{
			return jdbcTemplate.query(queryStr, new AdottanteRowMapper(), new Object[] { idAdottabile });
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	/**
	 * @param search
	 * @return List<Adottante>
	 */
	
	@Transactional()
	public List<Adottante> getAdottantiDisponibili(String search) {
		if(search == null || "".equals(search)) {
			search = Def.STR_PERCENTAGE;
		}else {
			search = Def.STR_PERCENTAGE + search + Def.STR_PERCENTAGE;
		}
		String queryStr = "Select " + 
				"    c.id_contatto, " + 
				"    c.nome, " + 
				"    c.cognome, " + 
				"    c.blacklist, " + 
				"    adn.id_adottante, " + 
				"    adn.wallet, " + 
				"    DATE_FORMAT(nullif(adn.dt_inserimento,''), '%d/%m/%Y') dt_inserimento, " + 
				"    adn.account, " + 
				"    adn.attivo, " + 
				"    IFNULL((Select Sum(md_adozioni.quota) From md_adozioni Where md_adozioni.id_adottante = adn.id_adottante), 0) wallet_occupato, " + 
				"    IFNULL(round((Select Sum(md_adozioni.quota) From md_adozioni  Where md_adozioni.id_adottante = adn.id_adottante) * 100 / adn.wallet,2),0) perc_occupato, " + 
				"    round(adn.wallet - IFNULL((Select Sum(md_adozioni.quota) From md_adozioni Where md_adozioni.id_adottante = adn.id_adottante),0),2) wallet_disponibile, " + 
				"    round((adn.wallet - IFNULL((Select Sum(md_adozioni.quota) From md_adozioni Where md_adozioni.id_adottante = adn.id_adottante),0)) * 100 / adn.wallet,2) perc_disponibile " + 
				"From " + 
				"    md_adottanti adn Inner Join " + 
				"    an_contatti c On adn.id_contatto = c.id_contatto " + 
				"Where " + 
				"    c.blacklist = 0 and concat(upper(c.nome), upper(c.cognome)) like upper(?) " +
				"    and adn.id_adottante in (select id_adottante from md_adottanti) " +
				"order by c.cognome";
		try{
			return jdbcTemplate.query(queryStr, new AdottanteRowMapper(), new Object[] { search });
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	/**
	 * @param search
	 * @return List<Adottante>
	 */
	
	@Transactional()
	public List<Adottante> getAdottantiWithAdozioni(String search) {
		if(search == null || "".equals(search)) {
			search = Def.STR_PERCENTAGE;
		}else {
			search = Def.STR_PERCENTAGE + search + Def.STR_PERCENTAGE;
		}
		String queryStr = "Select " + 
				"    c.id_contatto, " + 
				"    c.nome, " + 
				"    c.cognome, " + 
				"    c.blacklist, " + 
				"    adn.id_adottante, " + 
				"    adn.wallet, " + 
				"    DATE_FORMAT(nullif(adn.dt_inserimento,''), '%d/%m/%Y') dt_inserimento, " + 
				"    adn.account, " + 
				"    adn.attivo, " + 
				"    IFNULL((Select Sum(md_adozioni.quota) From md_adozioni Where md_adozioni.id_adottante = adn.id_adottante), 0) wallet_occupato, " + 
				"    IFNULL(round((Select Sum(md_adozioni.quota) From md_adozioni  Where md_adozioni.id_adottante = adn.id_adottante) * 100 / adn.wallet,2),0) perc_occupato, " + 
				"    round(adn.wallet - IFNULL((Select Sum(md_adozioni.quota) From md_adozioni Where md_adozioni.id_adottante = adn.id_adottante),0),2) wallet_disponibile, " + 
				"    round((adn.wallet - IFNULL((Select Sum(md_adozioni.quota) From md_adozioni Where md_adozioni.id_adottante = adn.id_adottante),0)) * 100 / adn.wallet,2) perc_disponibile " + 
				"From " + 
				"    md_adottanti adn Inner Join " + 
				"    an_contatti c On adn.id_contatto = c.id_contatto " + 
				"Where c.blacklist = 0 " +
				" and adn.id_adottante in (select id_adottante from md_adozioni)" +
				" and concat(upper(c.nome), upper(c.cognome)) like upper(?) order by c.cognome";
		try{
			return jdbcTemplate.query(queryStr, new AdottanteRowMapper(), new Object[] { search });
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	// @param id
	// @return List<Adottante>
	
	@Transactional()
	public Adottante getAdottanteWithAdozione(String id) {
		String queryStr = "Select " + 
				"    c.id_contatto, " + 
				"    c.nome, " + 
				"    c.cognome, " + 
				"    c.blacklist, " + 
				"    adn.id_adottante, " + 
				"    adn.wallet, " + 
				"    DATE_FORMAT(nullif(adn.dt_inserimento,''), '%d/%m/%Y') dt_inserimento, " + 
				"    adn.account, " + 
				"    adn.attivo, " + 
				"    IFNULL((Select Sum(md_adozioni.quota) From md_adozioni Where md_adozioni.id_adottante = adn.id_adottante), 0) wallet_occupato, " + 
				"    IFNULL(round((Select Sum(md_adozioni.quota) From md_adozioni  Where md_adozioni.id_adottante = adn.id_adottante) * 100 / adn.wallet,2),0) perc_occupato, " + 
				"    round(adn.wallet - IFNULL((Select Sum(md_adozioni.quota) From md_adozioni Where md_adozioni.id_adottante = adn.id_adottante),0),2) wallet_disponibile, " + 
				"    round((adn.wallet - IFNULL((Select Sum(md_adozioni.quota) From md_adozioni Where md_adozioni.id_adottante = adn.id_adottante),0)) * 100 / adn.wallet,2) perc_disponibile " + 
				"From " + 
				"    md_adottanti adn Inner Join " + 
				"    an_contatti c On adn.id_contatto = c.id_contatto " + 
				"Where c.blacklist = 0 " +
				" and adn.id_adottante = ?";
		try{
			return jdbcTemplate.queryForObject(queryStr, new AdottanteRowMapper(), new Object[] { id });
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	
	@Transactional
	public TotaleAdottanti getTotaliAdottanti() {
		
		String query = "Select " + 
				"	sum(adn.wallet) tot_wallet, " + 
				"    sum(round(adn.wallet - IFNULL((Select Sum(md_adozioni.quota) From md_adozioni Where md_adozioni.id_adottante = adn.id_adottante),0),2)) tot_wallet_disponibile,     " + 
				"    sum(IFNULL((Select Sum(md_adozioni.quota) From md_adozioni Where md_adozioni.id_adottante = adn.id_adottante), 0)) tot_wallet_occupato  " + 
				"From " + 
				"    md_adottanti adn Inner Join " + 
				"    an_contatti c On adn.id_contatto = c.id_contatto " + 
				"Where c.blacklist = 0";
				
		try{
			return jdbcTemplate.queryForObject(query, new TotaleAdottantiRowMapper());
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}		
	}
	
	
	/**
	 * @param search
	 * @return List<Adottabile>
	 */
	
	@Transactional()
	public List<Adottabile> getAdottabili(String search) {
		if(search == null || "".equals(search)) {
			search = Def.STR_PERCENTAGE;
		}else {
			search = Def.STR_PERCENTAGE + search + Def.STR_PERCENTAGE;
		}
		String queryStr = "Select " + 
				"    b.id_adottabile, " + 
				"    b.id_animale, " + 
				"    b.note, " + 
				"    DATE_FORMAT(nullif(b.dt_inserimento,''), '%d/%m/%Y') dt_inserimento, " + 
				"    b.account, " + 
				"    b.costo, " + 
				"    b.attivo, " + 
				"    a.cod_animale, " + 
				"    a.nome, " + 
				"    (select sum(quota) from md_adozioni where id_adottabile = b.id_adottabile) totQuota " +
				"From " + 
				"    an_animale a Inner Join " + 
				"    md_adottabili b On b.id_animale = a.id_animale " + 
				"Where upper(a.nome) like upper(?) " + 
				"Order by a.nome";
		try{
			return jdbcTemplate.query(queryStr, new AdottabileRowMapper(), new Object[] { search });
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	/**
	 * @param idAdottante
	 * @return List<Adottabile>
	 */
	
	@Transactional()
	public List<Adottabile> getAdottabiliByIdAdottante(String idAdottante) {
		String queryStr = "Select " + 
				"    b.id_adottabile, " + 
				"    b.id_animale, " + 
				"    b.note, " + 
				"    DATE_FORMAT(nullif(b.dt_inserimento,''), '%d/%m/%Y') dt_inserimento, " + 
				"    b.account, " + 
				"    b.costo, " + 
				"    b.attivo, " + 
				"    a.cod_animale, " + 
				"    a.nome, " + 
				"    (select sum(quota) from md_adozioni where id_adottabile = b.id_adottabile) totQuota " +
				" From " + 
				"    an_animale a Inner Join " + 
				"    md_adottabili b On b.id_animale = a.id_animale Inner Join " + 
				"    md_adozioni adz On adz.id_adottabile = b.id_adottabile " + 
				" Where " + 
				"    adz.id_adottante = ? " + 
				"Order by a.nome";
		try{
			return jdbcTemplate.query(queryStr, new AdottabileRowMapper(), new Object[] { idAdottante });
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	/**
	 * @param search
	 * @return List<Adottabile>
	 */
	
	@Transactional()
	public List<Adottabile> getAdottabiliDisponibili(String search) {
		if(search == null || "".equals(search)) {
			search = Def.STR_PERCENTAGE;
		}else {
			search = Def.STR_PERCENTAGE + search + Def.STR_PERCENTAGE;
		}
		String queryStr = "Select " + 
				"    b.id_adottabile, " + 
				"    b.id_animale, " + 
				"    b.note, " + 
				"    DATE_FORMAT(nullif(b.dt_inserimento,''), '%d/%m/%Y') dt_inserimento, " + 
				"    b.account, " + 
				"    b.costo, " + 
				"    b.attivo, " + 
				"    a.cod_animale, " + 
				"    a.nome, " + 
				"    (select sum(quota) from md_adozioni where id_adottabile = b.id_adottabile) totQuota " +
				"From " + 
				"    an_animale a Inner Join " + 
				"    md_adottabili b On b.id_animale = a.id_animale " + 
				"Where upper(a.nome) like upper(?) and b.id_adottabile not in (" +
				"	(select x.id_adottabile from " + 
				"		(Select  adl.id_adottabile,  (Sum(adz.quota)-adl.costo) diff " + 
				"			From  md_adottabili adl Inner Join  md_adozioni adz On adz.id_adottabile = adl.id_adottabile " + 
				"			group by adl.id_adottabile, adl.costo) x  " +
				"	where x.diff >= 0) " +
				") " + 
				"Order by a.nome";
		try{
			return jdbcTemplate.query(queryStr, new AdottabileRowMapper(), new Object[] { search });
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	/**
	 * @param idAdottante
	 * @return List<Adottabile>
	 */
	
	@Transactional()
	public List<Adottabile> getAdottabiliByID(String idAdottante) {

		String queryStr = "Select " + 
				"    b.id_adottabile, " + 
				"    b.id_animale, " + 
				"    b.note, " + 
				"    DATE_FORMAT(nullif(b.dt_inserimento,''), '%d/%m/%Y') dt_inserimento, " + 
				"    b.account, " + 
				"    b.costo, " + 
				"    b.attivo, " + 
				"    a.cod_animale, " + 
				"    a.nome, " + 
				"    (select sum(quota) from md_adozioni where id_adottabile = b.id_adottabile) totQuota " +
				" From " + 
				"    an_animale a Inner Join " + 
				"    md_adottabili b On b.id_animale = a.id_animale Inner Join " + 
				"    md_adozioni adz On adz.id_adottabile = b.id_adottabile" + 
				" Where adz.id_adottante = ? " + 
				" Order by a.nome";
		try{
			return jdbcTemplate.query(queryStr, new AdottabileRowMapper(), new Object[] { idAdottante });
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	private static class AdottanteRowMapper extends BaseRowMapper<Adottante> {
		public AdottanteRowMapper() {}		
		
		public Adottante mapRowImpl(ResultSet rs, int i) throws SQLException {
		
			Adottante o = new Adottante();
			o.setId_contatto(rs.getString("id_contatto"));
			o.setId_adottante(rs.getString("id_adottante"));
			o.setNome(rs.getString("nome"));
			o.setCognome(rs.getString("cognome"));
			o.setWallet(rs.getString("wallet"));
			o.setDt_inserimento(rs.getString("dt_inserimento"));
			o.setAttivo(rs.getString("attivo"));
			o.setBlacklist(rs.getString("blacklist"));
			o.setWallet_occupato(rs.getString("wallet_occupato"));
			o.setPerc_occupato(rs.getString("perc_occupato"));
			o.setWallet_disponibile(rs.getString("wallet_disponibile"));
			o.setPerc_disponibile(rs.getString("perc_disponibile"));
			o.setAccount(rs.getString("account"));
			return o;
		}
	}
	
	private static class AdottabileRowMapper extends BaseRowMapper<Adottabile> {
		public AdottabileRowMapper() {}		
		
		public Adottabile mapRowImpl(ResultSet rs, int i) throws SQLException {
		
			Adottabile o = new Adottabile();
			o.setDt_inserimento(rs.getString("dt_inserimento"));
			o.setAttivo(rs.getString("attivo"));
			o.setAccount(rs.getString("account"));
			o.setCod_animale(rs.getString("cod_animale"));
			o.setCosto(rs.getString("costo"));
			o.setId_adottabile(rs.getString("id_adottabile"));
			o.setId_animale(rs.getString("id_animale"));
			o.setNome(rs.getString("nome"));
			o.setNote(rs.getString("note"));
			o.setTotQuota(rs.getString("totQuota"));
			return o;
		}
	}
	
	private static class AdozioneRowMapper extends BaseRowMapper<Adozione> {
		public AdozioneRowMapper() {}		
		
		public Adozione mapRowImpl(ResultSet rs, int i) throws SQLException {
		
			Adozione o = new Adozione();
			o.setId_adozione(rs.getString("id_adozione"));
			o.setId_adottabile(rs.getString("id_adottabile"));
			o.setId_adottante(rs.getString("id_adottante"));
			o.setQuota(rs.getString("quota"));
			o.setAccount(rs.getString("account"));
			o.setDt_inserimento(rs.getString("dt_inserimento"));
			o.setNote(rs.getString("note"));
			o.setTotQuota(rs.getString("totQuote"));
			o.setAttivo(rs.getString("attivo"));
			return o;
		}
	}
	
	private static class TotaleAdottantiRowMapper extends BaseRowMapper<TotaleAdottanti> {
		public TotaleAdottantiRowMapper() {}		
		
		public TotaleAdottanti mapRowImpl(ResultSet rs, int i) throws SQLException {
		
			TotaleAdottanti o = new TotaleAdottanti();
			o.setTot_wallet(rs.getString("tot_wallet"));
			o.setTot_wallet_disponibile(rs.getString("tot_wallet_disponibile"));
			o.setTot_wallet_occupato(rs.getString("tot_wallet_occupato"));
			return o;
		}
	}
	
	/*-----------------------------------------------------------------------------------------------------------*/
	/**
	 * @param search
	 * @return List<Adozione>
	 */
	
	@Transactional()
	public List<Adozione> getAdozioni(String search) {
		search = "%" + search.toUpperCase() + "%";
		String queryStr = "Select Distinct " +
				"     adz.id_adozione, " +
				"     adz.id_adottante, " +
				"     adz.id_adottabile, " +
				"     adz.attivo, " +
				"     a.cod_animale, " +
				"     a.nome nome_animale, " +
				"     adz.account, " +
				"     DATE_FORMAT(nullif(adz.dt_inserimento,''), '%d/%m/%Y') dt_inserimento, " +
				"     Sum(adz.quota) quota, " +
				"     adz.note, " +
				"     (select sum(quota) from md_adozioni where id_adottabile = adz.id_adottabile) totQuote " +
				" From " +
				"     md_adozioni adz Left Join " +
				"     md_adottanti adn On adn.id_adottante = adz.id_adottante Left Join " +
				"     an_contatti c On c.id_contatto = adn.id_contatto Inner Join " +
				"     md_adottabili adl On adz.id_adottabile = adl.id_adottabile Inner Join " +
				"     an_animale a On adl.id_animale = a.id_animale " +
				" Where " +
				"     Upper(Concat(IfNull(c.nome, ''), IfNull(c.cognome, ''), IfNull(a.nome, ''))) Like (?) " +
				" Group By " +
				"     adz.id_adozione, " +
				"     adz.id_adottante, " +
				"     adz.id_adottabile, " +
				"     a.cod_animale, " +
				"     a.nome, " +
				"     adz.account, " +
				"     Date_Format(NullIf(adz.dt_inserimento, ''), '%d/%m/%Y'), " +
				"     adz.dt_inserimento " +
				" Order By " +
				"     nome_animale ";
		try{
			return jdbcTemplate.query(queryStr, new AdozioneRowMapper(), new Object[] { search });
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}

	
	/**
	 * @param id
	 * @return Adozione
	 */
	
	@Transactional()
	public Adozione getAdozioneByID(String id) {
		
		String queryStr = "Select " +
				"     adz.id_adozione, " +
				"     adz.id_adottante, " +
				"     adz.id_adottabile, " +
				"     adz.attivo, " +
				"     a.cod_animale, " +
				"     a.nome nome_animale, " +
				"     adz.account, " +
				"     DATE_FORMAT(nullif(adz.dt_inserimento,''), '%d/%m/%Y') dt_inserimento, " +
				"     adz.quota quota, " +
				"     adz.note, " +
				"     (select sum(quota) from md_adozioni where id_adottabile = adz.id_adottabile) totQuote " +
				" From " +
				"     md_adozioni adz Left Join " +
				"     md_adottanti adn On adn.id_adottante = adz.id_adottante Left Join " +
				"     an_contatti c On c.id_contatto = adn.id_contatto Inner Join " +
				"     md_adottabili adl On adz.id_adottabile = adl.id_adottabile Inner Join " +
				"     an_animale a On adl.id_animale = a.id_animale " +
				" Where " +
				"     adz.id_adozione = ? ";
		try{
			return jdbcTemplate.queryForObject(queryStr, new AdozioneRowMapper(), new Object[] { id });
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	/**
	 * @param idAdottante, idAdottabile
	 * @return Adozione
	 */
	
	@Transactional()
	public Adozione getAdozioneByAdottanteAndAdottabile(String idAdottante, String idAdottabile) {
		
		String queryStr = "Select " +
				"     adz.id_adozione, " +
				"     adz.id_adottante, " +
				"     adz.id_adottabile, " +
				"     adz.attivo, " +
				"     a.cod_animale, " +
				"     a.nome nome_animale, " +
				"     adz.account, " +
				"     DATE_FORMAT(nullif(adz.dt_inserimento,''), '%d/%m/%Y') dt_inserimento, " +
				"     adz.quota quota, " +
				"     adz.note, " +
				"     (select sum(quota) from md_adozioni where id_adottabile = adz.id_adottabile) totQuote " +
				" From " +
				"     md_adozioni adz Left Join " +
				"     md_adottanti adn On adn.id_adottante = adz.id_adottante Left Join " +
				"     an_contatti c On c.id_contatto = adn.id_contatto Inner Join " +
				"     md_adottabili adl On adz.id_adottabile = adl.id_adottabile Inner Join " +
				"     an_animale a On adl.id_animale = a.id_animale " +
				" Where " +
				"     adz.id_adottante = ? and adz.id_adottabile= ?" +
				" Group By " +
				"     adz.id_adozione, " +
				"     adz.id_adottante, " +
				"     adz.id_adottabile, " +
				"     a.cod_animale, " +
				"     a.nome, " +
				"     adz.account, " +
				"     Date_Format(NullIf(adz.dt_inserimento, ''), '%d/%m/%Y'), " +
				"     adz.dt_inserimento ";
		try{
			return jdbcTemplate.queryForObject(queryStr, new AdozioneRowMapper(), new Object[] { idAdottante, idAdottabile });
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	/**
	 * @param id
	 * @return List<Adottante>
	 */
	
	@Transactional()
	public List<Adottante> getAdottantiByID(String id) {
		
		String queryStr = "Select " +
				"     c.id_contatto, " +
				"     c.nome, " +
				"     c.cognome, " +
				"     c.blacklist, " +
				"     adn.id_adottante, " +
				"     adn.wallet, " +
				"     DATE_FORMAT(nullif(adn.dt_inserimento,''), '%d/%m/%Y') dt_inserimento, " +
				"     adn.account, " +
				"     adn.attivo, " +
				"     IFNULL((Select Sum(md_adozioni.quota) From md_adozioni Where md_adozioni.id_adottante = adn.id_adottante), 0) wallet_occupato,   " +
				"     IFNULL(round((Select Sum(md_adozioni.quota) From md_adozioni  Where md_adozioni.id_adottante = adn.id_adottante) * 100 / adn.wallet,2),0) perc_occupato,   " +
				"     round(adn.wallet - IFNULL((Select Sum(md_adozioni.quota) From md_adozioni Where md_adozioni.id_adottante = adn.id_adottante),0),2) wallet_disponibile,   " +
				"     round((adn.wallet - IFNULL((Select Sum(md_adozioni.quota) From md_adozioni Where md_adozioni.id_adottante = adn.id_adottante),0)) * 100 / adn.wallet,2) perc_disponibile  " +
				" From " +
				"     md_adottanti adn Inner Join " +
				"     an_contatti c On adn.id_contatto = c.id_contatto Inner Join " +
				"     md_adozioni adz On adz.id_adottante = adn.id_adottante " +
				" Where adz.id_adozione = ? ";
		try{
			return jdbcTemplate.query(queryStr, new AdottanteRowMapper(), new Object[] { id });
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	/**
	 * @param adozione
	 * @return
	 */
	
	@Transactional()
	public String saveOrUpdate(Adozione adozione) {
		String id = "";

		if (adozione.getId_adozione() == null) {
			id = save(adozione);
		} else {
			id = update(adozione);
		}
		return id;
	}
	
	/**
	 * @param adozione
	 * @return idAdozione
	 */
	@Transactional()
	private String save(Adozione adozione) {
		final String query = "INSERT INTO md_adozioni (id_adottante, id_adottabile, quota, account, note, dt_inserimento) " + 
				"VALUES (:id_adottante, :id_adottabile, :quota, :account, :note, str_to_date(nullif(:dt_inserimento,''), '%d/%m/%Y'))";

		KeyHolder keyHolder = new GeneratedKeyHolder();

		SqlParameterSource parameters = new BeanPropertySqlParameterSource(adozione);
        assert jdbcTemplate.getDataSource() != null;
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters, keyHolder, new String[] { "id_adozione" });

		return String.valueOf(keyHolder.getKey());
	}
	
	/**
	 * @param adozione
	 * @return
	 */

	
	@Transactional()
	public String update(Adozione adozione) {

		final String query = 
				"UPDATE md_adozioni " + 
				" SET " + 
				" id_adottante = :id_adottante," + 
				" id_adottabile = :id_adottabile," + 
				" quota = :quota, " +  
				" note = :note, " + 
				" account = :account, " + 
				" dt_inserimento = str_to_date(nullif(:dt_inserimento,''), '%d/%m/%Y') " + 
				"WHERE id_adozione = :id_adozione";

		SqlParameterSource parameters = new BeanPropertySqlParameterSource(adozione);
        assert jdbcTemplate.getDataSource() != null;
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters);

		return adozione.getId_adozione();
	}
	
	/**
	 * @param idAdozione
	 * @return
	 */
	
	@Transactional()
	public String updateAdozioneAttivazione(String attivo, String idAdozione) {

		final String query = 
				"UPDATE md_adozioni " + 
				" SET " + 
				" attivo = ? " +
				"WHERE id_adozione = ?";

		jdbcTemplate.update(query, new Object[] { attivo, idAdozione });

		return Def.STR_OK;
	}
	

	/*********************************************************************************************************/
	
	/**
	 * @param a
	 * @return
	 */
	
	@Transactional()
	public String saveOrUpdateAdottante(Adottante a) {
		String id = "";

		if (a.getId_adottante() == null) {
			id = saveAdottante(a);
		} else {
			id = updateAdottante(a);
		}
		return id;
	}
	
	/**
	 * @param a
	 * @return idAdozione
	 */
	@Transactional()
	private String saveAdottante(Adottante a) {
		final String query = "INSERT INTO md_adottanti (id_contatto, wallet, attivo, account) " + 
				"VALUES(:id_contatto, :wallet, :attivo, :account)";

		KeyHolder keyHolder = new GeneratedKeyHolder();

		SqlParameterSource parameters = new BeanPropertySqlParameterSource(a);
        assert jdbcTemplate.getDataSource() != null;
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters, keyHolder, new String[] { "id_adottante" });

		return String.valueOf(keyHolder.getKey());
	}
	
	/**
	 * @param a
	 * @return idAdottante
	 */
	
	@Transactional()
	public String updateAdottante(Adottante a) {
		final String query = 
				"UPDATE md_adottanti " + 
				"SET " + 
				"account = :account, " + 
				"id_contatto = :id_contatto, " + 
				"wallet = :wallet, attivo = :attivo " + 
				"WHERE id_adottante = :id_adottante";

		SqlParameterSource parameters = new BeanPropertySqlParameterSource(a);
        assert jdbcTemplate.getDataSource() != null;
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters);

		return a.getId_adottante();
	}
	
	/**
	 * @param idAdottante
	 * @return
	 */
	
	@Transactional()
	public String deleteByID(String idAdottante) throws SQLIntegrityConstraintViolationException {
		final String query = "DELETE FROM md_adottanti WHERE id_adottante = ?";
		jdbcTemplate.update(query, new Object[] { idAdottante });
		return Def.STR_OK;
	}
	
/*********************************************************************************************************/
	
	/**
	 * @param a
	 * @return
	 */
	
	@Transactional()
	public String saveOrUpdateAdottabile(Adottabile a) {
		String id = "";

		if (a.getId_adottabile() == null) {
			id = saveAdottabile(a);
		} else {
			id = updateAdottabile(a);
		}
		return id;
	}
	
	/**
	 * @param a
	 * @return idAdottabile
	 */
	@Transactional()
	private String saveAdottabile(Adottabile a) {
		final String query = "INSERT INTO md_adottabili (id_animale, costo, attivo, account) " + 
				"VALUES(:id_animale, :costo, :attivo, :account)";

		KeyHolder keyHolder = new GeneratedKeyHolder();

		SqlParameterSource parameters = new BeanPropertySqlParameterSource(a);
        assert jdbcTemplate.getDataSource() != null;
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters, keyHolder, new String[] { "id_adottabile" });

		return String.valueOf(keyHolder.getKey());
	}
	
	/**
	 * @param a
	 * @return idAdottabile
	 */

	
	@Transactional()
	public String updateAdottabile(Adottabile a) {
		final String query = 
				"UPDATE md_adottabili " + 
				"SET " + 
				"account = :account, " + 
				"id_animale = :id_animale, " + 
				"costo = :costo, attivo = :attivo, note = :note " + 
				"WHERE id_adottabile = :id_adottabile";

		SqlParameterSource parameters = new BeanPropertySqlParameterSource(a);
        assert jdbcTemplate.getDataSource() != null;
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters);

		return a.getId_adottabile();
	}
	
	/**
	 * @param idAdottabile
	 * @return
	 */
	
	@Transactional()
	public String deleteAdottabileByID(String idAdottabile) throws SQLIntegrityConstraintViolationException {
		final String query = "DELETE FROM md_adottabili WHERE id_adottabile = ?";
		jdbcTemplate.update(query, new Object[] { idAdottabile });
		return Def.STR_OK;
	}

	/**
	 * @param idAdozione
	 * @return List<Versamento>
	 */
	
	@Transactional()
	public List<Versamento> getVersamentiByAdozione(String idAdozione) {
		
		String queryStr = "SELECT  " + 
				"a.id_adozione,  " + 
				"v.id_versamento, " + 
				"a.id_adottante, " + 
				"v.importo, " + 
				"DATE_FORMAT(nullif(v.dt_versamento,''), '%d/%m/%Y') dt_versamento, " + 
				"v.account, " + 
				"DATE_FORMAT(nullif(v.dt_inserimento,''), '%d/%m/%Y') dt_inserimento  " + 
				"FROM md_adozioni a INNER JOIN md_versamenti v ON v.id_adozione = a.id_adozione " + 
				"WHERE a.id_adozione = ? ORDER BY v.dt_versamento desc";
		try{
			return jdbcTemplate.query(queryStr, new VersamentoRowMapper(), new Object[] { idAdozione });
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	/**
	 * @param id
	 * @return Versamento
	 */
	
	@Transactional()
	public Versamento getVersamentiById(String id) {
		
		String queryStr = "SELECT v.id_versamento,v.id_adozione,v.importo,DATE_FORMAT(nullif(v.dt_versamento,''), '%d/%m/%Y') dt_versamento,v.account,DATE_FORMAT(nullif(v.dt_inserimento,''), '%d/%m/%Y') dt_inserimento " + 
				"FROM md_versamenti v " + 
				" WHERE v.id_versamento = ? ";
		try{
			return jdbcTemplate.queryForObject(queryStr, new VersamentoRowMapper(), new Object[] { id });
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	/**
	 * @param a
	 * @return
	 */
	
	@Transactional()
	public String saveOrUpdateVersamento(Versamento a) {
		String id = "";

		if (a.getId_versamento() == null) {
			id = saveVersamento(a);
		} else {
			id = updateVersamento(a);
		}
		return id;
	}
	
	/**
	 * @param a
	 * @return idAdozione
	 */
	@Transactional()
	private String saveVersamento(Versamento a) {
		final String query = "INSERT INTO md_versamenti (id_adozione,importo,dt_versamento,account) " + 
								"VALUES (:id_adozione,:importo,str_to_date(nullif(:dt_versamento,''), '%d/%m/%Y'),:account)";

		KeyHolder keyHolder = new GeneratedKeyHolder();

		SqlParameterSource parameters = new BeanPropertySqlParameterSource(a);
        assert jdbcTemplate.getDataSource() != null;
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters, keyHolder, new String[] { "id_versamento" });

		return String.valueOf(keyHolder.getKey());
	}
	
	/**
	 * @param a
	 * @return idAdottante
	 */
	
	@Transactional()
	public String updateVersamento(Versamento a) {

		final String query = 
				"UPDATE md_versamenti " + 
				"SET " + 
				"importo = :importo, " + 
				"dt_versamento = str_to_date(nullif(:dt_versamento,''), '%d/%m/%Y') " + 
				"WHERE id_versamento = :id_versamento";

		SqlParameterSource parameters = new BeanPropertySqlParameterSource(a);
        assert jdbcTemplate.getDataSource() != null;
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters);

		return a.getId_adozione();
	}
	
	/**
	 * @param idVersamento
	 * @return
	 */
	
	@Transactional()
	public String deleteVersamentoByID(String idVersamento) throws SQLIntegrityConstraintViolationException {
		final String query = "DELETE FROM md_versamenti WHERE id_versamento = ?";
		jdbcTemplate.update(query, new Object[] { idVersamento });
		return Def.STR_OK;
	}
	
/*********************************************************************************************/
	
	/**
	 * @param anno
	 * @return List<Adozione>
	 */
	
	@Transactional()
	public List<Calendario> getCalendario(String anno) {
		
		String queryStr = "Select " +
				"     a.nome nome_animale, " +
				"     ad.attivo, " +
				"     ad.id_contatto, " +
				"     Concat(c.cognome, ' ', c.nome) nome_contatto, " +
				"     az.quota, " +
				"     az.dt_inserimento, " +
				"     a.id_animale, " +
				"     2019 anno, " +
				"     Sum(v.gennaio) As gennaio, " +
				"     Sum(v.febbraio) As febbraio, " +
				"     Sum(v.marzo) As marzo, " +
				"     Sum(v.aprile) As aprile, " +
				"     Sum(v.maggio) As maggio, " +
				"     Sum(v.giugno) As giugno, " +
				"     Sum(v.luglio) As luglio, " +
				"     Sum(v.agosto) As agosto, " +
				"     Sum(v.settembre) As settembre, " +
				"     Sum(v.ottobre) As ottobre, " +
				"     Sum(v.novembre) As novembre, " +
				"     Sum(v.dicembre) As dicembre, " +
				"     Sum((v.gennaio + v.febbraio + v.marzo + v.aprile + v.maggio + v.giugno + v.luglio + v.agosto + v.settembre + " +
				"     v.ottobre + v.novembre + v.dicembre)) totale " +
				" From " +
				"     md_adottanti ad Inner Join " +
				"     an_contatti c On ad.id_contatto = c.id_contatto Inner Join " +
				"     md_adozioni az On az.id_adottante = ad.id_adottante Inner Join " +
				"     md_adottabili On az.id_adottabile = md_adottabili.id_adottabile Inner Join " +
				"     an_animale a On md_adottabili.id_animale = a.id_animale Inner Join " +
				"     (Select " +
				"          v.id_adozione, " +
				"          Sum(v.importo * (1 - ABS(SIGN(Month(v.dt_versamento) - 1)))) gennaio, " +
				"          Sum(v.importo * (1 - ABS(SIGN(Month(v.dt_versamento) - 2)))) febbraio, " +
				"          Sum(v.importo * (1 - ABS(SIGN(Month(v.dt_versamento) - 3)))) marzo, " +
				"          Sum(v.importo * (1 - ABS(SIGN(Month(v.dt_versamento) - 4)))) aprile, " +
				"          Sum(v.importo * (1 - ABS(SIGN(Month(v.dt_versamento) - 5)))) maggio, " +
				"          Sum(v.importo * (1 - ABS(SIGN(Month(v.dt_versamento) - 6)))) giugno, " +
				"          Sum(v.importo * (1 - ABS(SIGN(Month(v.dt_versamento) - 7)))) luglio, " +
				"          Sum(v.importo * (1 - ABS(SIGN(Month(v.dt_versamento) - 8)))) agosto, " +
				"          Sum(v.importo * (1 - ABS(SIGN(Month(v.dt_versamento) - 9)))) settembre, " +
				"          Sum(v.importo * (1 - ABS(SIGN(Month(v.dt_versamento) - 10)))) ottobre, " +
				"          Sum(v.importo * (1 - ABS(SIGN(Month(v.dt_versamento) - 11)))) novembre, " +
				"          Sum(v.importo * (1 - ABS(SIGN(Month(v.dt_versamento) - 12)))) dicembre " +
				"      From " +
				"          md_versamenti v " +
				"      Where " +
				"          Year(v.dt_versamento) = ? " +
				"      Group By " +
				"          v.id_adozione) v On v.id_adozione = az.id_adozione " +
				" Group By " +
				"     nome_animale, " +
				"     nome_contatto With Rollup ";
		try{
			return jdbcTemplate.query(queryStr, new CalendarioRowMapper(), new Object[] { anno });
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	
	
	
	/**
	 * @param id
	 * @return List<Versamento>
	 */
	
	@Transactional()
	public List<Versamento> getVersamentiByAdottante(String id) {
		
		String queryStr = "SELECT v.id_versamento,v.id_adozione,v.importo,DATE_FORMAT(nullif(v.dt_versamento,''), '%d/%m/%Y') dt_versamento,v.account,DATE_FORMAT(nullif(v.dt_inserimento,''), '%d/%m/%Y') dt_inserimento " + 
				"FROM md_versamenti v " + 
				" WHERE v.id_adozione = ? ORDER BY v.dt_versamento desc";
		try{
			return jdbcTemplate.query(queryStr, new VersamentoRowMapper(), new Object[] { id });
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	
/*********************************************************************************************/
	
	
	private static class VersamentoRowMapper extends BaseRowMapper<Versamento> {
		public VersamentoRowMapper() {}		
		
		public Versamento mapRowImpl(ResultSet rs, int i) throws SQLException {
		
			Versamento o = new Versamento();
			o.setId_adozione(rs.getString("id_adozione"));
			o.setAccount(rs.getString("account"));
			o.setDt_inserimento(rs.getString("dt_inserimento"));
			o.setDt_versamento(rs.getString("dt_versamento"));
			o.setImporto(rs.getString("importo"));
			o.setId_versamento(rs.getString("id_versamento"));
			return o;
		}
	}
	
	private static class CalendarioRowMapper extends BaseRowMapper<Calendario> {
		public CalendarioRowMapper() {}		
		
		public Calendario mapRowImpl(ResultSet rs, int i) throws SQLException {
			Calendario o = new Calendario();
			o.setNome_contatto(rs.getString("nome_contatto"));
			o.setId_contatto(rs.getString("id_contatto"));
			o.setId_animale(rs.getString("id_animale"));
			o.setNome_animale(rs.getString("nome_animale"));
			o.setDt_donazione(rs.getString("dt_inserimento"));
			o.setAttivo(rs.getString("attivo"));
			o.setAnno(rs.getString("anno"));
			o.setTotale(rs.getString("totale"));
			o.setGennaio(rs.getString("gennaio"));
			o.setFebbraio(rs.getString("febbraio"));
			o.setMarzo(rs.getString("marzo"));
			o.setAprile(rs.getString("aprile"));
			o.setMaggio(rs.getString("maggio"));
			o.setGiugno(rs.getString("giugno"));
			o.setLuglio(rs.getString("luglio"));
			o.setAgosto(rs.getString("agosto"));
			o.setSettembre(rs.getString("settembre"));
			o.setOttobre(rs.getString("ottobre"));
			o.setNovembre(rs.getString("novembre"));
			o.setDicembre(rs.getString("dicembre"));
			o.setQuota(rs.getString("quota"));
			return o;
		}
	}

	
}
