package it.asso.core.dao.animali.animale;

import it.asso.core.common.Def;
import it.asso.core.common.Utils;
import it.asso.core.dao.common.BaseRowMapper;
import it.asso.core.dao.contatto.ContattoDAO;
import it.asso.core.dao.documenti.DocumentoDAO;
import it.asso.core.model.animali.animale.Animale;
import it.asso.core.model.animali.animale.RicercaDTO;
import it.asso.core.model.contatto.Contatto;
import it.asso.core.model.statistiche.SimpleResultSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Year;
import java.util.Collections;
import java.util.List;



@Repository
public class AnimaleDAO {
    
    private static Logger logger = LoggerFactory.getLogger(AnimaleDAO.class);


    private final JdbcTemplate jdbcTemplate;
    private final ContattoDAO contattoDao;
    private final DocumentoDAO documentoDao;

    // ✅ Iniezione via costruttore (più chiaro e testabile)
    public AnimaleDAO(JdbcTemplate jdbcTemplate, ContattoDAO contattoDao, DocumentoDAO documentoDao) {
        this.jdbcTemplate = jdbcTemplate;
        this.contattoDao = contattoDao;
        this.documentoDao = documentoDao;
    }


    /**
	 * @param id
	 * @return Animale
	 */
	@Transactional(readOnly = true)
	public Animale getById(String id) {
		
		String queryStr = "SELECT a.id_animale, cod_animale, " + 
				"    a.nome, " + 
				"    NullIf(DATE_FORMAT(a.dt_nascita,'%d/%m/%Y'),'') dt_nascita, " + 
				"    a.num_microchip, " + 
				"    a.peso, " + 
				"    a.id_colore, " + 
				"    a.id_tipo_animale, " + 
				"    a.id_stato, a.caratteristiche, b.descr_stato, a.sesso, a.id_razza, a.tipo_razza, r.razza, a.descr_breve, a.descr_lunga " + 
				" FROM an_animale a join (an_x_stati b) on (a.id_stato=b.id_stato)  left join (an_x_razze r) on (a.id_razza = r.id_razza) " + 
				" WHERE id_animale = ?";

		try{
			return (Animale) jdbcTemplate.queryForObject(queryStr, new AnimaleRowMapper(), new Object[] { id });

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	/**
	 * @param id
	 * @return String
	 */
	@Transactional(readOnly = true)
	public String getNomeById(String id) {
		
		String queryStr = "SELECT a.nome FROM an_animale a  WHERE id_animale = ?";

		try{
			return jdbcTemplate.queryForObject(queryStr, String.class, new Object[] { id });

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	/**
	 * @param strToSearch
	 * @return List<Animael>
	 */
	@Transactional(readOnly = true)
	public List<Animale> getBySearch(String strToSearch) {
		
		strToSearch =  Def.STR_PERCENTAGE + strToSearch.trim() +  Def.STR_PERCENTAGE;
		String queryStr = "SELECT a.id_animale,  cod_animale," + 
				"    a.nome, " + 
				"    NullIf(DATE_FORMAT(a.dt_nascita,'%d/%m/%Y'),'') dt_nascita, " + 
				"    a.num_microchip, " + 
				"    a.peso, " + 
				"    a.id_colore, " + 
				"    a.id_tipo_animale, a.caratteristiche, b.descr_stato, a.sesso, a.id_stato, a.id_razza, a.tipo_razza, r.razza, a.descr_breve, a.descr_lunga " + 
				" FROM an_animale a join (an_x_stati b) on (a.id_stato=b.id_stato)  left join (an_x_razze r) on (a.id_razza = r.id_razza) " + 
				" WHERE upper(CONCAT(ifnull(a.nome,''),ifnull(a.num_microchip,''))) like (?) ORDER BY id_animale desc";

		try{
			return jdbcTemplate.query(queryStr, new AnimaleRowMapper(), new Object[] { strToSearch.toUpperCase() });

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	/**
	 * @param strToSearch
	 * @return List<Animael>
	 */
	@Transactional(readOnly = true)
	public List<Animale> getAdottabiliBySearch(String strToSearch) {
		
		strToSearch =  Def.STR_PERCENTAGE + strToSearch.trim() +  Def.STR_PERCENTAGE;
		String queryStr = "SELECT a.id_animale,  cod_animale," + 
				"    a.nome, " + 
				"    NullIf(DATE_FORMAT(a.dt_nascita,'%d/%m/%Y'),'') dt_nascita, " + 
				"    a.num_microchip, " + 
				"    a.peso, " + 
				"    a.id_colore, " + 
				"    a.id_tipo_animale, a.caratteristiche, b.descr_stato, a.sesso, a.id_stato, a.id_razza, a.tipo_razza, r.razza, a.descr_breve, a.descr_lunga " + 
				" FROM an_animale a join (an_x_stati b) on (a.id_stato=b.id_stato)  left join (an_x_razze r) on (a.id_razza = r.id_razza) " + 
				" WHERE upper(CONCAT(ifnull(a.nome,''),ifnull(a.num_microchip,''))) like (?) and a.id_stato in (3,4) " +
				" and a.id_animale not in (select id_animale from md_adottabili) " +
				" ORDER BY id_animale desc";

		try{
			return jdbcTemplate.query(queryStr, new AnimaleRowMapper(), new Object[] { strToSearch.toUpperCase() });

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	/**
	 * @param 
	 * @return List<Animael>
	 */
	@Transactional(readOnly = true)
	public List<Animale> getRandom() {
		
		String queryStr = "SELECT a.id_animale,  cod_animale," + 
				"    a.nome, " + 
				"    NullIf(DATE_FORMAT(a.dt_nascita,'%d/%m/%Y'),'') dt_nascita, " + 
				"    a.num_microchip, " + 
				"    a.peso, " + 
				"    a.id_colore, " + 
				"    a.id_tipo_animale, a.caratteristiche, b.descr_stato, a.sesso, a.id_stato, a.id_razza, a.tipo_razza, r.razza, a.descr_breve, a.descr_lunga " + 
				" FROM an_animale a join (an_x_stati b) on (a.id_stato=b.id_stato)  left join (an_x_razze r) on (a.id_razza = r.id_razza) " + 
				" WHERE a.id_stato in (3,4) " + 
				//" ORDER BY RAND()";
				" ORDER BY RAND() LIMIT 0,9";

		try{
			return jdbcTemplate.query(queryStr, new AnimaleRowMapper());

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	/**
	 * @param anno
	 * @return List<Animael>
	 */
	@Transactional(readOnly = true)
	public List<Animale> getLietiFine(String anno) {
		
		String queryStr = "select id_animale, dt_inserimento, nome, sesso, provincia from ( " + 
				"Select a.id_animale, Max(a.dt_inserimento) As dt_inserimento, b.nome, b.sesso, c.provincia " + 
				"From " + 
				"    (Select Distinct an_r_iter.id_animale, Max(an_documenti.dt_inserimento) As dt_inserimento " + 
				"     From an_r_iter Inner Join an_r_iter_documenti On an_r_iter_documenti.id_iter = an_r_iter.id_iter Inner Join " + 
				"         an_documenti On an_r_iter_documenti.id_documento = an_documenti.id_documento " + 
				"     Where an_r_iter.id_tipo_iter = 4 And an_documenti.id_tipo_documento = 11 " + 
				"     Group By an_r_iter.id_animale " + 
				"     union " + 
				"     Select an_r_iter.id_animale, an_r_iter.dt_colloquio From an_r_iter " + 
				"     Where an_r_iter.id_tipo_iter = 2 And an_r_iter.dt_colloquio Is Not Null " + 
				"     union " + 
				"     Select an_r_iter.id_animale, an_r_iter.dt_consegna From an_r_iter " + 
				"     Where an_r_iter.id_tipo_iter = 3 And an_r_iter.dt_consegna Is Not Null) a Inner Join " + 
				"    an_animale b On b.id_animale = a.id_animale Inner Join " + 
				"    an_r_iter r On r.id_animale = b.id_animale Inner Join " + 
				"    an_contatti c On c.id_contatto = r.id_contatto_adottante " + 
				"where r.id_pratica in ( select max(id_pratica) from as_pratiche group by id_animale) " + 
				"Group By a.id_animale, b.nome, b.sesso, c.provincia) x " + 
				"    where year(dt_inserimento) = ?";

		try{
			return jdbcTemplate.query(queryStr,  new AnimaleLightRowMapper(), new Object[] { anno });

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	/**
	 * @param 
	 * @return String
	 */
	@Transactional(readOnly = true)
	public String getLietiFineCount() {
		
		String queryStr = "select count(id_animale)   " + 
				"from ( " + 
				"select id_animale, max(dt_inserimento) as dt_inserimento " + 
				"from ( " + 
				"Select Distinct an_r_iter.id_animale, Max(an_documenti.dt_inserimento) As dt_inserimento " + 
				"From an_r_iter Inner Join an_r_iter_documenti On an_r_iter_documenti.id_iter = an_r_iter.id_iter Inner Join " + 
				"    an_documenti On an_r_iter_documenti.id_documento = an_documenti.id_documento " + 
				"Where an_r_iter.id_tipo_iter = 4 And an_documenti.id_tipo_documento = 11 " + 
				"Group By an_r_iter.id_animale " + 
				"union " + 
				"SELECT id_animale, dt_colloquio FROM an_r_iter where id_tipo_iter = 2 and dt_colloquio is not null " + 
				"union " + 
				"SELECT id_animale, dt_consegna FROM an_r_iter where id_tipo_iter = 3 and dt_consegna is not null) a " + 
				"group by id_animale) b ";
		try{
			return jdbcTemplate.queryForObject(queryStr, String.class);

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	/**
	 * @param 
	 * @return String
	 */
	@Transactional(readOnly = true)
	public List<SimpleResultSet> getLietiFineCountByAnno() {
		
		String queryStr = "select count(id_animale) count, year(dt_inserimento) anno   " + 
				"from ( " + 
				"select id_animale, max(dt_inserimento) as dt_inserimento " + 
				"from ( " + 
				"Select Distinct an_r_iter.id_animale, Max(an_documenti.dt_inserimento) As dt_inserimento " + 
				"From an_r_iter Inner Join an_r_iter_documenti On an_r_iter_documenti.id_iter = an_r_iter.id_iter Inner Join " + 
				"    an_documenti On an_r_iter_documenti.id_documento = an_documenti.id_documento " + 
				"Where an_r_iter.id_tipo_iter = 4 And an_documenti.id_tipo_documento = 11 " + 
				"Group By an_r_iter.id_animale " + 
				"union " + 
				"SELECT id_animale, dt_colloquio FROM an_r_iter where id_tipo_iter = 2 and dt_colloquio is not null " + 
				"union " + 
				"SELECT id_animale, dt_consegna FROM an_r_iter where id_tipo_iter = 3 and dt_consegna is not null) a " + 
				"group by id_animale) b " + 
				"group by year(dt_inserimento)";

		try{
			return jdbcTemplate.query(queryStr, new SimpleResultSetRowMapper());

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	
	/**
	 * @param tipo, eta, taglia, sesso, regione, provincia
	 * @return List<Animael>
	 */
	@Transactional(readOnly = true)
	public List<Animale> getRicercaPubblica(String tipo, String eta, String taglia, String sesso, String regione, String provincia) {
		logger.info("tipo: " +  tipo + " eta: " + eta + " taglia: " + taglia + " sesso: " + sesso + " regione: " + regione + " provincia: " + provincia);
		if(eta != null && !"".equals(eta)) {eta = " like ('" + eta + "%') ";}else {eta = " like ('%')";}
		if(taglia != null && !"".equals(taglia)) {taglia = " = '" + taglia + "' ";}else {
			if(Def.NUM_DUE.equals(tipo)) {
					taglia = " is null";
			}else if ("".equals(tipo)) {
				taglia = " is null or v.taglia like ('%') ";
			}else{
					taglia = " like ('%')";
			}
		}
		if(sesso != null && !"".equals(sesso)) {sesso = " = '" + sesso + "' ";}else {sesso = " like ('%')";}
		if(tipo != null && !"".equals(tipo)) {tipo = " = '" + tipo + "' ";}else {tipo = " like ('%')";}
		
		if(provincia != null && !"".equals(provincia)) {provincia = " = '" + provincia + "' ";}else {provincia = " like ('%')";}
		
		if(regione != null && !"".equals(regione)) {regione = " = '" + regione + "' ";}else {regione = " like ('%')";}
		
		
		String queryStr = "SELECT " + 
				"a.id_animale, a.cod_animale, a.nome, NullIf(DATE_FORMAT(a.dt_nascita,'%d/%m/%Y'),'') dt_nascita, " + 
				"a.num_microchip, a.peso, a.id_colore ,a.id_tipo_animale,a.caratteristiche,b.descr_stato,a.sesso, " + 
				"a.id_stato,a.id_razza,a.tipo_razza,r.razza,a.descr_breve,a.descr_lunga,c.provincia, c.id_provincia, c.id_regione " + 
				"FROM " + 
				"  an_animale a " + 
				"  JOIN (v_animali v) ON a.id_animale = v.id_animale " + 
				"  JOIN (an_x_stati b) ON a.id_stato = b.id_stato " + 
				"  LEFT JOIN (an_x_razze r) ON a.id_razza = r.id_razza " + 
				"  LEFT JOIN v_last_eventi_storici ve ON a.id_animale = ve.id_animale " + 
				"  INNER JOIN an_contatti c ON c.id_contatto = ve.id_contatto " + 
				"WHERE a.id_stato in (3, 4) " + 
				"		 and v.eta " + eta +
				"        and (v.taglia " + taglia + ") " +
				"        and a.sesso " + sesso + 
				"        and a.id_tipo_animale " + tipo + 
				"        and c.id_provincia " + provincia +
				"        and c.id_regione " + regione +
				" ORDER BY a.nome ";


		try{
			logger.info(queryStr);
			return jdbcTemplate.query(queryStr, new AnimaleRowMapper());

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	/**
	 * @param ricerca
	 * @return List<Animael>
	 */
	@Transactional(readOnly = true)
	public List<Animale> getRicerca(RicercaDTO ricerca) {
		StringBuilder query = new StringBuilder();
		// Usiamo MapSqlParameterSource per gestire i parametri in modo flessibile
		MapSqlParameterSource params = new MapSqlParameterSource();

		query.append("SELECT DISTINCT a.id_animale, a.cod_animale, a.nome, ")
				.append("NullIf(DATE_FORMAT(a.dt_nascita,'%d/%m/%Y'),'') dt_nascita, ")
				.append("a.num_microchip, a.peso, a.id_colore, a.id_tipo_animale, ")
				.append("a.caratteristiche, b.descr_stato, a.sesso, a.id_stato, ")
				.append("a.id_razza, a.tipo_razza, r.razza, f.regione ")
				.append("FROM an_animale a ")
				.append("JOIN an_x_stati b ON a.id_stato = b.id_stato ")
				.append("LEFT JOIN an_x_razze r ON a.id_razza = r.id_razza ")
				.append("LEFT JOIN v_last_eventi_storici v ON a.id_animale = v.id_animale ")
				.append("LEFT JOIN an_contatti f ON f.id_contatto = v.id_contatto ")
				.append("LEFT JOIN an_r_animale_tags t ON t.id_animale = a.id_animale ")
				.append("WHERE 1=1 "); // Trucco per aggiungere "AND" facilmente

		// 1. Ricerca Nome/Microchip (Sanitizzazione inclusa)
		String searchVal = (ricerca.getSearch() == null || ricerca.getSearch().trim().isEmpty())
				? "%" : "%" + ricerca.getSearch().trim().toUpperCase() + "%";
		query.append("AND UPPER(CONCAT(IFNULL(a.nome,''), IFNULL(a.num_microchip,''))) LIKE :search ");
		params.addValue("search", searchVal);

		// 2. Filtro Sesso (Gestione pulita senza concatenazione)
		if (ricerca.getSesso() != null && !ricerca.getSesso().isEmpty()) {
			if (Def.NUM_MENO_UNO.equals(ricerca.getSesso())) {
				query.append("AND a.sesso IS NOT NULL ");
			} else {
				query.append("AND a.sesso = :sesso ");
				params.addValue("sesso", ricerca.getSesso());
			}
		}

		// 3. Età (Uso del CASE parameterizzato)
		String etaParam = (ricerca.getEta() == null || ricerca.getEta().isEmpty())
				? "%" : ricerca.getEta();

		query.append(" AND (CASE ")
				.append("    WHEN TIMESTAMPDIFF(MONTH, a.dt_nascita, NOW()) <= 11 THEN 'CUCCIOLO' ")
				.append("    WHEN TIMESTAMPDIFF(MONTH, a.dt_nascita, NOW()) <= 60 THEN 'ADULTO GIOVANE' ")
				.append("    WHEN TIMESTAMPDIFF(MONTH, a.dt_nascita, NOW()) <= 120 THEN 'ADULTO' ")
				.append("    ELSE 'ANZIANO' ")
				.append(" END) LIKE :eta ");

		params.addValue("eta", etaParam);

		// 4. Vaccini (Subquery sicura)
		String vacciniInput = ricerca.getVaccini();

		if (vacciniInput != null && !vacciniInput.isEmpty()) {
			query.append(" AND (SELECT Count(d.id_tipo_evento_clinico) ")
					.append("      FROM an_r_evento_clinico c ")
					.append("      INNER JOIN an_x_evento_clinico d ON c.id_tipo_evento = d.id_tipo_evento ")
					.append("      WHERE c.id_animale = a.id_animale AND d.id_tipo_evento_clinico = 1) ");

			if (">2".equals(vacciniInput)) {
				// Se l'utente ha scelto ">2", aggiungiamo l'operatore matematico direttamente
				query.append(" > 2 ");
			} else {
				// Altrimenti (casi "1" o "2"), usiamo il segnaposto sicuro
				query.append(" = :vaccini ");
				params.addValue("vaccini", Integer.parseInt(vacciniInput));
			}
		}

		// 5. Altri filtri (Regione, Provincia, Tag, Stato, Tipo)
		if (ricerca.getTag() != null && !ricerca.getTag().isEmpty()) {
			query.append("AND t.id_tag = :tag ");
			params.addValue("tag", ricerca.getTag());
		}

		if (ricerca.getRegione() != null && !ricerca.getRegione().isEmpty()) {
			query.append("AND f.id_regione = :regione ");
			params.addValue("regione", ricerca.getRegione());
		}

		query.append("AND a.id_stato LIKE :stato ")
				.append("AND a.id_tipo_animale LIKE :tipoAnimale ");

		params.addValue("stato", Utils.isOtherThenPercentage(ricerca.getStato()));
		params.addValue("tipoAnimale", Utils.isOtherThenPercentage(ricerca.getTipoAnimale()));

		// 6. Ordinamento e Paginazione
		query.append("ORDER BY a.id_animale DESC ");

		if (ricerca.getLimit() != null && ricerca.getOffset() != null) {
			query.append(" LIMIT :limit, :offset ");
			params.addValue("limit", ricerca.getLimit());
			params.addValue("offset", ricerca.getOffset());
		}


		try {
			NamedParameterJdbcTemplate namedTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
			return namedTemplate.query(query.toString(), params, new LastAnimaliRowMapper());
		} catch (EmptyResultDataAccessException e) {
			logger.error("Nessun risultato trovato: {}", e.getMessage());
			return Collections.emptyList(); // Meglio una lista vuota che null
		}
	}
	

	/**
	 * Restituisce il numero totale di animali che corrispondono ai criteri di ricerca.
	 * Fondamentale per la paginazione della tabella in Angular.
	 */
	@Transactional(readOnly = true)
	public int getCountBySearch(RicercaDTO ricerca) {
		StringBuilder query = new StringBuilder();
		MapSqlParameterSource params = new MapSqlParameterSource();

		// Usiamo COUNT(DISTINCT ...) per riflettere esattamente i risultati della query di ricerca
		query.append("SELECT COUNT(DISTINCT a.id_animale) ")
				.append("FROM an_animale a ")
				.append("JOIN an_x_stati b ON a.id_stato = b.id_stato ")
				.append("LEFT JOIN v_last_eventi_storici v ON a.id_animale = v.id_animale ")
				.append("LEFT JOIN an_contatti f ON f.id_contatto = v.id_contatto ")
				.append("LEFT JOIN an_r_animale_tags t ON t.id_animale = a.id_animale ")
				.append("WHERE 1=1 ");

		// --- STESSA LOGICA DI FILTRO DEL METODO getRicerca ---

		// 1. Ricerca Nome/Microchip
		String searchVal = (ricerca.getSearch() == null || ricerca.getSearch().trim().isEmpty())
				? "%" : "%" + ricerca.getSearch().trim().toUpperCase() + "%";
		query.append("AND UPPER(CONCAT(IFNULL(a.nome,''), IFNULL(a.num_microchip,''))) LIKE :search ");
		params.addValue("search", searchVal);

		// 2. Sesso
		if (ricerca.getSesso() != null && !ricerca.getSesso().isEmpty()) {
			if (Def.NUM_MENO_UNO.equals(ricerca.getSesso())) {
				query.append("AND a.sesso IS NOT NULL ");
			} else {
				query.append("AND a.sesso = :sesso ");
				params.addValue("sesso", ricerca.getSesso());
			}
		}

		// 3. Età
		String etaParam = (ricerca.getEta() == null || ricerca.getEta().isEmpty())
				? "%" : ricerca.getEta();

		query.append(" AND (CASE ")
				.append("    WHEN TIMESTAMPDIFF(MONTH, a.dt_nascita, NOW()) <= 11 THEN 'CUCCIOLO' ")
				.append("    WHEN TIMESTAMPDIFF(MONTH, a.dt_nascita, NOW()) <= 60 THEN 'ADULTO GIOVANE' ")
				.append("    WHEN TIMESTAMPDIFF(MONTH, a.dt_nascita, NOW()) <= 120 THEN 'ADULTO' ")
				.append("    ELSE 'ANZIANO' ")
				.append(" END) LIKE :eta ");

		params.addValue("eta", etaParam);

		// 4. Vaccini
		String vacciniInput = ricerca.getVaccini();

		if (vacciniInput != null && !vacciniInput.isEmpty()) {
			query.append(" AND (SELECT Count(d.id_tipo_evento_clinico) ")
					.append("      FROM an_r_evento_clinico c ")
					.append("      INNER JOIN an_x_evento_clinico d ON c.id_tipo_evento = d.id_tipo_evento ")
					.append("      WHERE c.id_animale = a.id_animale AND d.id_tipo_evento_clinico = 1) ");

			if (">2".equals(vacciniInput)) {
				// Se l'utente ha scelto ">2", aggiungiamo l'operatore matematico direttamente
				query.append(" > 2 ");
			} else {
				// Altrimenti (casi "1" o "2"), usiamo il segnaposto sicuro
				query.append(" = :vaccini ");
				params.addValue("vaccini", Integer.parseInt(vacciniInput));
			}
		}

		// 5. Filtri Geografici, Tag e Stato
		if (ricerca.getTag() != null && !ricerca.getTag().isEmpty()) {
			query.append("AND t.id_tag = :tag ");
			params.addValue("tag", ricerca.getTag());
		}

		if (ricerca.getRegione() != null && !ricerca.getRegione().isEmpty()) {
			query.append("AND f.id_regione = :regione ");
			params.addValue("regione", ricerca.getRegione());
		}

		query.append("AND a.id_stato LIKE :stato ")
				.append("AND a.id_tipo_animale LIKE :tipoAnimale ");

		params.addValue("stato", Utils.isOtherThenPercentage(ricerca.getStato()));
		params.addValue("tipoAnimale", Utils.isOtherThenPercentage(ricerca.getTipoAnimale()));



		try {
			NamedParameterJdbcTemplate namedTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
			Integer count = namedTemplate.queryForObject(query.toString(), params, Integer.class);
			return count != null ? count : 0;
		} catch (Exception e) {
			logger.error("Errore nel conteggio ricerca: {}", e.getMessage());
			return 0;
		}
	}
	
	/**
	 * @param numToList, String filterBy
	 * @return List<Animael>
	 */
	@Transactional(readOnly = true)
	public List<Animale> getLastInsert(int numToList, String filterBy) {
		String whereClause = "";
		
		if(Def.NUM_UNO.equals(filterBy)) {
			whereClause = "WHERE b.id_stato < 5";
		}else if(Def.NUM_DUE.equals(filterBy)) {
			whereClause = "WHERE b.id_stato >= 5";
		}
		String queryStr = "SELECT a.id_animale,a.cod_animale,a.nome,NullIf(DATE_FORMAT(a.dt_nascita,'%d/%m/%Y'),'') dt_nascita,a.num_microchip,a.peso,a.id_colore,a.id_tipo_animale, " + 
				"a.caratteristiche,b.descr_stato,a.sesso,a.id_stato,a.id_razza,a.tipo_razza,r.razza,c.regione " + 
				"FROM " + 
				"  an_animale a " + 
				"  LEFT JOIN (an_x_stati b) ON a.id_stato = b.id_stato " + 
				"  LEFT JOIN (an_x_razze r) ON a.id_razza = r.id_razza " + 
				"  LEFT JOIN v_last_eventi_storici v ON a.id_animale = v.id_animale " + 
				"  LEFT JOIN an_contatti c ON v.id_contatto = c.id_contatto  " + 
					whereClause +
				"  ORDER BY id_animale desc LIMIT ? ";

		try{
			return jdbcTemplate.query(queryStr, new LastAnimaliRowMapper(), new Object[] { numToList });

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	/**
	 * @param id
	 * @return String
	 */
	@Transactional(readOnly = true)
	public String getStato(String id) {
		
		String queryStr = "SELECT id_stato " + 
				" FROM an_animale " + 
				" WHERE id_animale = ?";

		try{
			return  jdbcTemplate.queryForObject(queryStr, String.class, new Object[] { id });

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	/**
	 * @param id
	 * @return String
	 */
	@Transactional(readOnly = true)
	public String getLocation(String id) {
		
		String queryStr = "SELECT" + 
				"	  concat(b.provincia, ' in ', b.regione) as location " + 
				"	FROM " + 
				"	  v_last_eventi_storici a " + 
				"	  INNER JOIN an_contatti b ON b.id_contatto = a.id_contatto " + 
				" WHERE a.id_animale = ?";

		try{
			return  jdbcTemplate.queryForObject(queryStr, String.class, new Object[] { id });

		} catch (EmptyResultDataAccessException e) {
			logger.error("ID Animale {} - " + e.getMessage(), id);
			return "";
		}
	}
	
	  
	  
	  
	/**
	 * @param microchip , idAnimale
	 * @return String
	 */
	@Transactional(readOnly = true)
	public int checkMicrochip(String microchip, String idAnimale) {
		
		String queryStr = "SELECT count(num_microchip) " + 
				" FROM an_animale " + 
				" WHERE num_microchip = ? and (id_animale != ? or id_animale is null)";

		try{
			return  jdbcTemplate.queryForObject(queryStr,  Integer.class, new Object[] { microchip, idAnimale });

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return 0;
		}
	}
	
	/**
	 * @param microchip
	 * @return String
	 */
	@Transactional(readOnly = true)
	public int checkMicrochip(String microchip) {
		
		String queryStr = "SELECT count(num_microchip) " + 
				" FROM an_animale " + 
				" WHERE num_microchip = ?";

		try{
			return  jdbcTemplate.queryForObject(queryStr,  Integer.class, new Object[] { microchip });

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return 0;
		}
	}
	
	/**
	 * @param 
	 * @return String
	 */
	@Transactional(readOnly = true)
	private String getCodice() {
		
		String queryStr = "SELECT ifnull(max(CONVERT(SUBSTRING(cod_animale, 7, 5),UNSIGNED INTEGER)),0) + 1 FROM an_animale ";
		
		try{
			int count = jdbcTemplate.queryForObject(queryStr, Integer.class);
			int anno = Year.now().getValue() - 2000;
			String prefisso = jdbcTemplate.queryForObject("SELECT sigla FROM org_anagrafica WHERE id_organizzazione = 1", String.class);
			String codice = jdbcTemplate.queryForObject("SELECT concat('" + prefisso + ".','" + anno + ".',LPAD(" + count + ",5,'0'))", String.class);
			return  codice;

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return Def.STR_KO;
		}
	}
	
	
	
	/**
	 * @param animale
	 * @return
	 */
	
	@Transactional()
	public String saveOrUpdate(Animale animale) {
		String idAnimale = "";

		if (animale.getId_animale() == null) {
			idAnimale = save(animale);
		} else {
			idAnimale = update(animale);
		}

		return idAnimale;
	}

	/**
	 * @param animale
	 * @return
	 */
	
	@Transactional()
	public String save(Animale animale) {
		animale.setCod_animale(getCodice());
		final String query = "INSERT INTO an_animale " + 
				"(nome,  cod_animale, " + 
				"dt_nascita," + 
				"num_microchip," + 
				"peso," + 
				"id_colore," + 
				"id_tipo_animale, caratteristiche, sesso, id_razza, tipo_razza)" + 
				" VALUES " + 
				"(upper(:nome), :cod_animale," + 
				"str_to_date(nullif(:dt_nascita,''),'%d/%m/%Y')," + 
				":num_microchip," + 
				"nullif(:peso,'')," + 
				"nullif(:id_colore,'')," + 
				"nullif(:id_tipo_animale,'')," +
				"nullif(:caratteristiche,'')," +
				"nullif(:sesso,'')," +
				"nullif(:id_razza,'')," +
				"nullif(:tipo_razza,'')" +
				")";

		KeyHolder keyHolder = new GeneratedKeyHolder();

		SqlParameterSource parameters = new BeanPropertySqlParameterSource(animale);
        assert jdbcTemplate.getDataSource() != null;
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters, keyHolder, new String[] { "id_animale" });

		return String.valueOf(keyHolder.getKey());
	}
	
	/**
	 * @param idAnimale
	 * @return Contatto
	 */
	@Transactional(readOnly = true)
	public Contatto getProprietarioByIDAnimale(String idAnimale) {
		Contatto contatto = new Contatto();
		String queryStr = "SELECT id_contatto_proprietario FROM an_animale_proprietari WHERE id_animale = ? and corrente = 1 ";
		try{
			List<String> listID = jdbcTemplate.queryForList(queryStr, String.class, new Object[] { idAnimale });
			if(!listID.isEmpty()) {
				contatto = contattoDao.getByID(listID.get(0), true);
				contatto.setDocumenti(documentoDao.getDocumentiByIDContatto(contatto.getId_contatto()));
				return contatto;
			}else {
				return null;
			}
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}

	@Transactional(readOnly = true)
	public String getDtProprietario(String idAnimale, String idContatto) {
		String query = "SELECT nullif(DATE_FORMAT(dt_inserimento, '%d/%m/%Y'), '') " +
				"FROM an_animale_proprietari " +
				"WHERE id_animale = ? AND id_contatto_proprietario = ? AND corrente = 1";
		try {
			return jdbcTemplate.queryForObject(query, String.class, idAnimale, idContatto);
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	/**
	 * @param idAnimale, String idContatto, String dtA
	 * @return String
	 */
	@Transactional()
	public String updateProprietario(String idAnimale, String idContatto, String dtA) {
		String query = "select count(*) from an_animale_proprietari where id_animale = ? and id_contatto_proprietario = ?";
		int count = jdbcTemplate.queryForObject(query, Integer.class, new Object[] { idAnimale, idContatto });
		if(count == 0) {
			query = "update an_animale_proprietari set corrente = 0 where id_animale = ?";
			jdbcTemplate.update(query, new Object[] { idAnimale });
			
			query = "insert into an_animale_proprietari (id_animale, id_contatto_proprietario, dt_inserimento, corrente) VALUES (?, ?, str_to_date(nullif(?,''),'%d/%m/%Y'), 1) ";
			jdbcTemplate.update(query, new Object[] { idAnimale, idContatto, dtA });
		}
		return Def.STR_OK;
	}
	
	/**
	 * @param idAnimale, String idPratica
	 * @return String
	 */
	@Transactional()
	public String updateProprietario(String idAnimale, String idPratica) {
		String query = "Select i.id_contatto_adottante " + 
				"From " + 
				"    an_r_iter i Inner Join " + 
				"    an_r_iter_documenti id On id.id_iter = i.id_iter Inner Join " + 
				"    an_documenti d On id.id_documento = d.id_documento " + 
				"Where " + 
				"    i.id_animale = ? And " + 
				"    i.id_pratica = ? And " + 
				"    i.id_tipo_iter = 4 And " + 
				"    d.id_tipo_documento = 13";
		String idContatto = jdbcTemplate.queryForObject(query, String.class, new Object[] { idAnimale, idPratica });
		
		if(idContatto != null) {
			query = "update an_animale_proprietari set corrente = 0 where id_animale = ?";
			jdbcTemplate.update(query, new Object[] { idAnimale });
			
			query = "insert into an_animale_proprietari (id_animale, id_contatto_proprietario, corrente) VALUES (?, ?, 1) ";
			jdbcTemplate.update(query, new Object[] { idAnimale, idContatto });
			
			return Def.STR_OK;
		}else {
			return Def.STR_KO;
		}
	}

	/**
	 * @param animale
	 * @return
	 */
	
	@Transactional()
	public String update(Animale animale) {

		final String query = "UPDATE an_animale " + 
				"SET " + 
				"nome = upper(:nome), " + 
				"dt_nascita = str_to_date(nullif(:dt_nascita,''),'%d/%m/%Y')," + 
				"num_microchip = :num_microchip, " + 
				"peso = :peso, " + 
				"id_colore = :id_colore, " + 
				"id_tipo_animale = :id_tipo_animale, " + 
				"caratteristiche = :caratteristiche, " +
				"sesso = :sesso, " +
				"id_razza = :id_razza, " +
				"tipo_razza = :tipo_razza, " +
				"descr_breve = :descr_breve, " +
				"descr_lunga = :descr_lunga " +
				"WHERE id_animale = :id_animale";

		SqlParameterSource parameters = new BeanPropertySqlParameterSource(animale);
        assert jdbcTemplate.getDataSource() != null;
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters);

		return animale.getId_animale();
	}
	
	/**
	 * @param id
	 * @return
	 */
	
	@Transactional()
	public void deleteByID(String id) {
		String query = "DELETE FROM an_animale WHERE id_animale = ?";
		jdbcTemplate.update(query, new Object[] { id });
		
//		query = "DELETE FROM an_r_animali_documenti WHERE id_animale = ?";
//		jdbcTemplate.update(query, new Object[] { id });
//		
//		query = "DELETE FROM an_r_animale_tags WHERE id_animale = ?";
//		jdbcTemplate.update(query, new Object[] { id });
//		
//		query = "DELETE FROM an_r_animale_tags WHERE id_animale = ?";
//		jdbcTemplate.update(query, new Object[] { id });
//		
//		query = "DELETE FROM an_r_animale_tags WHERE id_animale = ?";
//		jdbcTemplate.update(query, new Object[] { id });
		
	}
	
	
	private static class AnimaleRowMapper extends BaseRowMapper<Animale> {
		public AnimaleRowMapper() {}		
		@Override
		public Animale mapRowImpl(ResultSet rs, int i) throws SQLException {
		
			Animale o = new Animale();
			o.setId_animale(rs.getString("id_animale"));
			o.setNome(rs.getString("nome"));
			o.setNum_microchip(rs.getString("num_microchip"));
			o.setPeso(rs.getString("peso"));
			o.setId_colore(rs.getString("id_colore"));
			o.setId_tipo_animale(rs.getString("id_tipo_animale"));
			o.setDt_nascita(rs.getString("dt_nascita"));
			o.setCaratteristiche(rs.getString("caratteristiche"));
			o.setDescr_stato(rs.getString("descr_stato"));
			o.setId_stato(rs.getString("id_stato"));
			o.setSesso(rs.getString("sesso"));
			o.setId_razza(rs.getString("id_razza"));
			o.setTipo_razza(rs.getString("tipo_razza"));
			o.setRazza(rs.getString("razza"));
			o.setCod_animale(rs.getString("cod_animale"));
			o.setDescr_breve(rs.getString("descr_breve"));
			o.setDescr_lunga(rs.getString("descr_lunga"));
			return o;
		}
	}
	
	private static class AnimaleLightRowMapper extends BaseRowMapper<Animale> {
		public AnimaleLightRowMapper() {}		
		@Override
		public Animale mapRowImpl(ResultSet rs, int i) throws SQLException {
		
			Animale o = new Animale();
			o.setId_animale(rs.getString("id_animale"));
			o.setNome(rs.getString("nome"));
			o.setLocation(rs.getString("provincia"));
			o.setSesso(rs.getString("sesso"));
			return o;
		}
	}
	
	private static class SimpleResultSetRowMapper extends BaseRowMapper<SimpleResultSet> {
		public SimpleResultSetRowMapper() {}		
		@Override
		public SimpleResultSet mapRowImpl(ResultSet rs, int i) throws SQLException {
		
			SimpleResultSet o = new SimpleResultSet();
			o.setValue_1(rs.getString("count"));
			o.setValue_2(rs.getString("anno"));
			return o;
		}
	}
	
	private static class LastAnimaliRowMapper extends BaseRowMapper<Animale> {
		public LastAnimaliRowMapper() {}		
		@Override
		public Animale mapRowImpl(ResultSet rs, int i) throws SQLException {
		
			Animale o = new Animale();
			o.setId_animale(rs.getString("id_animale"));
			o.setNome(rs.getString("nome"));
			o.setNum_microchip(rs.getString("num_microchip"));
			o.setPeso(rs.getString("peso"));
			o.setId_colore(rs.getString("id_colore"));
			o.setId_tipo_animale(rs.getString("id_tipo_animale"));
			o.setDt_nascita(rs.getString("dt_nascita"));
			o.setCaratteristiche(rs.getString("caratteristiche"));
			o.setDescr_stato(rs.getString("descr_stato"));
			o.setId_stato(rs.getString("id_stato"));
			o.setSesso(rs.getString("sesso"));
			o.setId_razza(rs.getString("id_razza"));
			o.setTipo_razza(rs.getString("tipo_razza"));
			o.setRazza(rs.getString("razza"));
			o.setCod_animale(rs.getString("cod_animale"));
			o.setRegione(rs.getString("regione"));
			return o;
		}
	}
	
	
	

}
