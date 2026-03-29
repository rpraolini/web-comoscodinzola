package it.asso.core.dao.magazzino;

import it.asso.core.common.Def;
import it.asso.core.dao.common.BaseRowMapper;
import it.asso.core.model.magazzino.*;
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
public class MagazzinoDAO{

private static Logger logger = LoggerFactory.getLogger(MagazzinoDAO.class);


    private final JdbcTemplate jdbcTemplate;

    public MagazzinoDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
	
	
	
	/************************************* PRODOTTI **********************************************************/
	/**
	 * @param ricerca
	 * @return List<Prodotto>
	 */
	@Transactional(readOnly = true)
	public List<Prodotto> getProdottiByID(RicercaDTO ricerca) {
		String forPagination = "";
		
		if(ricerca.getLimit() != null && ricerca.getOffset() != null && !"".equals(ricerca.getLimit()) && !"".equals(ricerca.getOffset())) {
			forPagination = "limit " + ricerca.getLimit() + "," + ricerca.getOffset();
		}
		
		String queryStr = "Select "
				+ "    p.id_prodotto, "
				+ "    p.descr_prodotto, "
				+ "    p.id_tipologia, "
				+ "    p.id_sotto_tipologia, "
				+ "    p.id_marca, "
				+ "    p.prezzo, "
				+ "    p.immagine, "
				+ "    p.account, "
				+ "    NullIf(Date_Format(p.dt_aggiornamento, '%d/%m/%Y %H:%i:%s'), '') dt_aggiornamento, "
				+"     (Select sum(quantita) From mg_r_prodotto_magazzino where id_prodotto = p.id_prodotto) numPezzi "
				+ " From "
				+ "    mg_prodotti p "
				+ " Where p.id_tipologia = ? " + forPagination;
		try{
			return jdbcTemplate.query(queryStr, new ProdottoRowMapper(), new Object[] {ricerca.getIdTipologia()});

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	/**
	 * @param ricerca
	 * @return int
	 */
	@Transactional(readOnly = true)
	public int getCountProdottiByID(RicercaDTO ricerca) {
		
		String queryStr = "Select count(*) "
				+ " From "
				+ "    mg_prodotti p Inner Join "
				+ "    mg_r_prodotto_magazzino pm On pm.id_prodotto = p.id_prodotto Inner Join "
				+ "    mg_x_magazzino m On pm.id_magazzino = m.id_magazzino "
				+ " Where p.id_tipologia = ? ";
		try{
			return jdbcTemplate.queryForObject(queryStr, Integer.class, new Object[] {ricerca.getIdTipologia()});

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return 0;
		}
	}
	
	/**
	 * @param  prodotto
	 * @return String
	 */
	@Transactional()
	public String saveOrUpdateProdotto(Prodotto prodotto) throws SQLIntegrityConstraintViolationException {
		String idProdotto = "";

		if (prodotto.getIdProdotto() == null) {
			idProdotto = save(prodotto);
			logger.info("Inserito prodotto: " + idProdotto + " " + prodotto.getDescrProdotto() );
		} else {
			idProdotto = update(prodotto);
		}
		
		return idProdotto;
	}
	
	/**
	 * @param prodotto
	 * @return idProdotto
	 */
	@Transactional()
	private String save(Prodotto prodotto) {
		if("".equalsIgnoreCase(prodotto.getPrezzo())) {
			prodotto.setPrezzo(null);
		}
		String query = "INSERT INTO mg_prodotti (descr_prodotto,id_tipologia,id_sotto_tipologia,id_marca,prezzo,immagine,account) "
				+ "VALUES (:descrProdotto,:idTipologia,:idSottoTipologia,:idMarca,:prezzo,:immagine,:account)";

		KeyHolder keyHolder = new GeneratedKeyHolder();
		
		SqlParameterSource parameters = new BeanPropertySqlParameterSource(prodotto);
        assert jdbcTemplate.getDataSource() != null;
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters, keyHolder);

		return String.valueOf(keyHolder.getKey());
	}
	
	/**
	 * @param prodotto
	 * @return idProdotto
	 */
	@Transactional()
	private String update(Prodotto prodotto) {
		if("".equalsIgnoreCase(prodotto.getPrezzo())) {
			prodotto.setPrezzo(null);
		}
		String query = "UPDATE mg_prodotti "
				+ "SET "
				+ "descr_prodotto = :descrProdotto, "
				+ "id_tipologia = :idTipologia, "
				+ "id_sotto_tipologia = :idSottoTipologia, "
				+ "id_marca = :idMarca, "
				+ "prezzo = :prezzo, "
				+ "immagine = :immagine, "
				+ "dt_aggiornamento = NOW() "
				+ "WHERE id_prodotto = :idProdotto";

		SqlParameterSource parameters = new BeanPropertySqlParameterSource(prodotto);
        assert jdbcTemplate.getDataSource() != null;
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters);
		
		return prodotto.getIdProdotto();
	}
	
	/**
	 * @param  prodotto
	 * @return
	 */
	@Transactional()
	public String delete(Prodotto prodotto) {
		String query = "DELETE FROM mg_r_prodotto_magazzino where id_prodotto = ?";
		jdbcTemplate.update(query, new Object[] { prodotto.getIdProdotto() });
		query = "DELETE FROM mg_prodotti WHERE id_prodotto = ?";
		jdbcTemplate.update(query, new Object[] {  prodotto.getIdProdotto() });
		return Def.STR_OK;
	}
	
	/************************************* MAGAZZINI **********************************************************/
	/**
	 * @param 
	 * @return List<Magazzino>
	 */
	@Transactional(readOnly = true)
	public List<Magazzino> getMagazzini() {
		
		String queryStr = "Select id_magazzino, descr_magazzino " + 
				" FROM mg_x_magazzino";
		try{
			return jdbcTemplate.query(queryStr, new MagazzinoRowMapper());

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	/**
	 * @param  idMagazzino
	 * @return Magazzino
	 */
	@Transactional(readOnly = true)
	public Magazzino getMagazzinoByID(String idMagazzino) {
		
		String queryStr = "Select id_magazzino, descr_magazzino " + 
				" FROM mg_x_magazzino where id_magazzino = ?";
		try{
			return jdbcTemplate.queryForObject(queryStr, new MagazzinoRowMapper(), new Object[] {idMagazzino});

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	/**
	 * @param  ricerca
	 * @return List<Magazzino>
	 */
	@Transactional(readOnly = true)
	public List<Magazzino> getMagazziniRicerca(RicercaDTO ricerca) {
		String forPagination = "";
		
		if(ricerca.getLimit() != null && ricerca.getOffset() != null && !"".equals(ricerca.getLimit()) && !"".equals(ricerca.getOffset())) {
			forPagination = "limit " + ricerca.getLimit() + "," + ricerca.getOffset();
		}
		
		String queryStr = "Select id_magazzino, descr_magazzino " +
								" FROM mg_x_magazzino order by descr_magazzino " + forPagination;
		try{
			return jdbcTemplate.query(queryStr, new MagazzinoRowMapper());

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	/**
	 * @param
	 * @return int
	 */
	@Transactional(readOnly = true)
	public int getCountMagazzini() {
		
		String queryStr = "Select count(*)  From  mg_x_magazzino ";
		try{
			return jdbcTemplate.queryForObject(queryStr, Integer.class);

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return 0;
		}
	}
	
	/**
	 * @param  magazzino
	 * @return String
	 */
	@Transactional()
	public String saveOrUpdateMagazzino(Magazzino magazzino) throws SQLIntegrityConstraintViolationException {
		String idMagazzino = "";

		if (magazzino.getIdMagazzino() == null) {
			idMagazzino = save(magazzino);
			logger.info("Inserito taglia: " + idMagazzino + " " + magazzino.getDescrMagazzino() );
		} else {
			idMagazzino = update(magazzino);
		}
		
		return idMagazzino;
	}
	
	/**
	 * @param  magazzino
	 * @return idMagazzino 
	 */
	@Transactional()
	private String save(Magazzino magazzino) {

		String query = "INSERT INTO mg_x_magazzino "
				+ "(descr_magazzino) "
				+ "VALUES (upper(:descrMagazzino))";

		KeyHolder keyHolder = new GeneratedKeyHolder();
		
		SqlParameterSource parameters = new BeanPropertySqlParameterSource(magazzino);
		NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters, keyHolder);

		return String.valueOf(keyHolder.getKey());
	}
	
	/**
	 * @param  magazzino
	 * @return idMagazzino
	 */
	@Transactional()
	private String update(Magazzino magazzino) {
		String query = "UPDATE mg_x_magazzino "
				+ "SET "
				+ "descr_magazzino = upper(:descrMagazzino) "
				+ "WHERE id_magazzino = :idMagazzino";

		SqlParameterSource parameters = new BeanPropertySqlParameterSource(magazzino);
        assert jdbcTemplate.getDataSource() != null;
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters);
		
		return magazzino.getIdMagazzino();
	}
	
	/**
	 * @param magazzino
	 * @return
	 */
	@Transactional()
	public String delete(Magazzino magazzino) {
		String ret = "";
		String query = "SELECT count(*) FROM mg_r_prodotto_magazzino where id_magazzino = ?";
		int count = jdbcTemplate.queryForObject(query, Integer.class, new Object[] {magazzino.getIdMagazzino()});
		ret = Def.STR_ERROR_501;
		if(count == 0) {
			query = "DELETE from mg_x_magazzino WHERE id_magazzino = :idMagazzino";
	
			SqlParameterSource parameters = new BeanPropertySqlParameterSource(magazzino);
			NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
			namedParameterJdbcTemplate.update(query, parameters);
			ret = Def.STR_OK;
		}
		return ret;
	}
	
	/************************************* COLORI **********************************************************/
	/**
	 * @param 
	 * @return List<Colore>
	 */
	@Transactional(readOnly = true)
	public List<Colore> getColori() {
		
		String queryStr = "Select id_colore, descr_colore " + 
				" FROM mg_x_colori";
		try{
			return jdbcTemplate.query(queryStr, new ColoreRowMapper());

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	/**
	 * @param idColore
	 * @return Colore
	 */
	@Transactional(readOnly = true)
	public Colore getColoreByID(String idColore) {
		
		String queryStr = "Select id_colore, descr_colore " + 
							" FROM mg_x_colori where id_colore = ?";
		try{
			return jdbcTemplate.queryForObject(queryStr, new ColoreRowMapper(), new Object[] {idColore});

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	/**
	 * @param ricerca
	 * @return List<Colore>
	 */
	@Transactional(readOnly = true)
	public List<Colore> getColori(RicercaDTO ricerca) {
		String forPagination = "";
		
		if(ricerca.getLimit() != null && ricerca.getOffset() != null && !"".equals(ricerca.getLimit()) && !"".equals(ricerca.getOffset())) {
			forPagination = "limit " + ricerca.getLimit() + "," + ricerca.getOffset();
		}
		
		String queryStr = "Select id_colore, descr_colore " +
								" FROM mg_x_colori order by descr_colore " + forPagination;
		try{
			return jdbcTemplate.query(queryStr, new ColoreRowMapper());

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	/**
	 * @param
	 * @return int
	 */
	@Transactional(readOnly = true)
	public int getCountColori() {
		
		String queryStr = "Select count(*)  From  mg_x_colori ";
		try{
			return jdbcTemplate.queryForObject(queryStr, Integer.class);

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return 0;
		}
	}
	
	/**
	 * @param  colore
	 * @return String
	 */
	@Transactional()
	public String saveOrUpdateColore(Colore colore) throws SQLIntegrityConstraintViolationException {
		String idMarca = "";

		if (colore.getIdColore() == null) {
			idMarca = save(colore);
			logger.info("Inserito colore: " + idMarca + " " + colore.getDescrColore() );
		} else {
			idMarca = update(colore);
		}
		
		return idMarca;
	}
	
	/**
	 * @param  colore
	 * @return idColore
	 */
	@Transactional()
	private String save(Colore colore) {

		String query = "INSERT INTO mg_x_colori "
				+ "(descr_colore) "
				+ "VALUES (upper(:descrColore))";

		KeyHolder keyHolder = new GeneratedKeyHolder();
		
		SqlParameterSource parameters = new BeanPropertySqlParameterSource(colore);
		NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters, keyHolder);

		return String.valueOf(keyHolder.getKey());
	}
	
	/**
	 * @param colore
	 * @return idColore
	 */
	@Transactional()
	private String update(Colore colore) {
		String query = "UPDATE mg_x_colori "
				+ "SET "
				+ "descr_colore = upper(:descrColore) "
				+ "WHERE id_colore = :idColore";

		SqlParameterSource parameters = new BeanPropertySqlParameterSource(colore);
		NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters);
		
		return colore.getIdColore();
	}
	
	/**
	 * @param colore
	 * @return
	 */
	@Transactional()
	public String delete(Colore colore) {
		String ret = "";
		String query = "SELECT count(*) FROM mg_r_prodotto_magazzino where id_colore = ?";
		int count = jdbcTemplate.queryForObject(query, Integer.class, new Object[] {colore.getIdColore()});
		ret = Def.STR_ERROR_501;
		if(count == 0) {
			query = "DELETE from mg_x_colori WHERE id_colore = :idColore";
	
			SqlParameterSource parameters = new BeanPropertySqlParameterSource(colore);
			NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
			namedParameterJdbcTemplate.update(query, parameters);
			ret = Def.STR_OK;
		}
		return ret;
	}
	
	/************************************* TAGLIE **********************************************************/
	/**
	 * @param 
	 * @return List<Taglia>
	 */
	@Transactional(readOnly = true)
	public List<Taglia> getTaglie() {
		
		String queryStr = "Select id_taglia, descr_taglia " + 
				" FROM mg_x_taglie";
		try{
			return jdbcTemplate.query(queryStr, new TagliaRowMapper());

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	/**
	 * @param  idTaglia
	 * @return Taglia
	 */
	@Transactional(readOnly = true)
	public Taglia getTagliaByID(String idTaglia) {
		
		String queryStr = "Select id_taglia, descr_taglia " + 
				" FROM mg_x_taglie where id_taglia = ?";
		try{
			return jdbcTemplate.queryForObject(queryStr, new TagliaRowMapper(), new Object[] {idTaglia});

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	/**
	 * @param  ricerca
	 * @return List<Taglia>
	 */
	@Transactional(readOnly = true)
	public List<Taglia> getTaglie(RicercaDTO ricerca) {
		String forPagination = "";
		
		if(ricerca.getLimit() != null && ricerca.getOffset() != null && !"".equals(ricerca.getLimit()) && !"".equals(ricerca.getOffset())) {
			forPagination = "limit " + ricerca.getLimit() + "," + ricerca.getOffset();
		}
		
		String queryStr = "Select id_taglia, descr_taglia " +
								" FROM mg_x_taglie order by descr_taglia " + forPagination;
		try{
			return jdbcTemplate.query(queryStr, new TagliaRowMapper());

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	/**
	 * @param
	 * @return int
	 */
	@Transactional(readOnly = true)
	public int getCountTaglie() {
		
		String queryStr = "Select count(*)  From  mg_x_taglie ";
		try{
			return jdbcTemplate.queryForObject(queryStr, Integer.class);

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return 0;
		}
	}
	
	/**
	 * @param taglia
	 * @return String
	 */
	@Transactional()
	public String saveOrUpdateTaglia(Taglia taglia) throws SQLIntegrityConstraintViolationException {
		String idTaglia = "";

		if (taglia.getIdTaglia() == null) {
			idTaglia = save(taglia);
			logger.info("Inserito taglia: " + idTaglia + " " + taglia.getDescrTaglia() );
		} else {
			idTaglia = update(taglia);
		}
		
		return idTaglia;
	}
	
	/**
	 * @param taglia
	 * @return idTaglia
	 */
	@Transactional()
	private String save(Taglia taglia) {

		String query = "INSERT INTO mg_x_taglie "
				+ "(descr_taglia) "
				+ "VALUES (upper(:descrTaglia))";

		KeyHolder keyHolder = new GeneratedKeyHolder();
		
		SqlParameterSource parameters = new BeanPropertySqlParameterSource(taglia);
		NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters, keyHolder);

		return String.valueOf(keyHolder.getKey());
	}
	
	/**
	 * @param taglia
	 * @return idTaglia
	 */
	@Transactional()
	private String update(Taglia taglia) {
		String query = "UPDATE mg_x_taglie "
				+ "SET "
				+ "descr_taglia = upper(:descrTaglia) "
				+ "WHERE id_taglia = :idTaglia";

		SqlParameterSource parameters = new BeanPropertySqlParameterSource(taglia);
		NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters);
		
		return taglia.getIdTaglia();
	}
	
	/**
	 * @param taglia
	 * @return
	 */
	@Transactional()
	public String delete(Taglia taglia) {
		String ret = "";
		String query = "SELECT count(*) FROM mg_r_prodotto_magazzino where id_taglia = ?";
		int count = jdbcTemplate.queryForObject(query, Integer.class, new Object[] {taglia.getIdTaglia()});
		ret = Def.STR_ERROR_501;
		if(count == 0) {
			query = "DELETE from mg_x_taglie WHERE id_taglia = :idTaglia";
	
			SqlParameterSource parameters = new BeanPropertySqlParameterSource(taglia);
			NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
			namedParameterJdbcTemplate.update(query, parameters);
			ret = Def.STR_OK;
		}
		return ret;
	}
	
	/************************************* PRODOTTO MAGAZZINO **********************************************************/
	
	/**
	 * @param idProdotto
	 * @return List<MagazzinoProdotto>
	 */
	@Transactional(readOnly = true)
	public List<MagazzinoProdotto> getMagazzinoProdottoByID(String idProdotto) {
		
		String queryStr = "Select "
				+ "    pm.id, "
				+ "    pm.id_prodotto, "
				+ "    pm.id_magazzino, "
				+ "    pm.quantita, "
				+ "    pm.id_taglia, "
				+ "    pm.id_colore, "
				+ "    m.descr_magazzino, "
				+ "    t.descr_taglia, "
				+ "    c.descr_colore "
				+ "From "
				+ "    mg_r_prodotto_magazzino pm Inner Join "
				+ "    mg_x_magazzino m On pm.id_magazzino = m.id_magazzino Inner Join "
				+ "    mg_x_taglie t On pm.id_taglia = t.id_taglia Inner Join "
				+ "    mg_x_colori c On pm.id_colore = c.id_colore "
				+ "Where "
				+ "    pm.id_prodotto = ? "
				+ "Order By "
				+ "    c.descr_colore, "
				+ "    t.descr_taglia, "
				+ "    m.descr_magazzino";
		try{
			return jdbcTemplate.query(queryStr, new MagazzinoProdottoRowMapper(), new Object[] {idProdotto});

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	/**
	 * @param mp
	 * @return id
	 */
	@Transactional()
	public String saveOrUpdateMagazzinoProdotto(MagazzinoProdotto mp) throws SQLIntegrityConstraintViolationException {
		String id = "";

		if (mp.getId() == null) {
			id = save(mp);
		} else {
			id = update(mp);
		}
		
		return id;
	}
	
	/**
	 * @param mp
	 * @return id
	 */
	@Transactional()
	private String save(MagazzinoProdotto mp) {

		String query = "INSERT INTO mg_r_prodotto_magazzino "
				+ "(id_prodotto,id_magazzino,quantita,id_taglia,id_colore) "
				+ "VALUES "
				+ "(:idProdotto,:idMagazzino,:quantita,:idTaglia,:idColore)";

		KeyHolder keyHolder = new GeneratedKeyHolder();
		
		SqlParameterSource parameters = new BeanPropertySqlParameterSource(mp);
		NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters, keyHolder);

		return String.valueOf(keyHolder.getKey());
	}
	
	/**
	 * @param mp
	 * @return id
	 */
	@Transactional()
	private String update(MagazzinoProdotto mp) {

		String query = "UPDATE mg_r_prodotto_magazzino "
				+ "SET "
				+ "id_prodotto = :idProdotto, "
				+ "id_magazzino = :idMagazzino, "
				+ "quantita = :quantita, "
				+ "id_taglia = :idTaglia, "
				+ "id_colore = :idColore "
				+ "WHERE id = :id";

		SqlParameterSource parameters = new BeanPropertySqlParameterSource(mp);
		NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters);
		
		return mp.getId();
	}
	
	/**
	 * @param mp
	 * @return
	 */
	@Transactional()
	public String delete(MagazzinoProdotto mp) {

		String query = "DELETE from mg_r_prodotto_magazzino WHERE id = :id";

		SqlParameterSource parameters = new BeanPropertySqlParameterSource(mp);
		NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters);
		
		return mp.getId();
	}
	
	/************************************* MARCHE **********************************************************/
	/**
	 * @param 
	 * @return List<Marca>
	 */
	@Transactional(readOnly = true)
	public List<Marca> getMarche() {
		
		String queryStr = "Select id_marca, descr_marca " + 
				" FROM mg_x_marche order by descr_marca";
		try{
			return jdbcTemplate.query(queryStr, new MarcaRowMapper());

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	/**
	 * @param ricerca
	 * @return List<Marca>
	 */
	@Transactional(readOnly = true)
	public List<Marca> getMarche(RicercaDTO ricerca) {
		String forPagination = "";
		
		if(ricerca.getLimit() != null && ricerca.getOffset() != null && !"".equals(ricerca.getLimit()) && !"".equals(ricerca.getOffset())) {
			forPagination = "limit " + ricerca.getLimit() + "," + ricerca.getOffset();
		}
		
		String queryStr = "Select id_marca, descr_marca " +
								" FROM mg_x_marche order by descr_marca " + forPagination;
		try{
			return jdbcTemplate.query(queryStr, new MarcaRowMapper());

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	/**
	 * @param idMarca
	 * @return Marca
	 */
	@Transactional(readOnly = true)
	public Marca getMarcaByID(String idMarca) {
		
		String queryStr = "Select id_marca, descr_marca " + 
				" FROM mg_x_marche where id_marca = ?";
		try{
			return jdbcTemplate.queryForObject(queryStr, new MarcaRowMapper(), new Object[] {idMarca});

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	/**
	 * @param
	 * @return int
	 */
	@Transactional(readOnly = true)
	public int getCountMarche() {
		
		String queryStr = "Select count(*)  From  mg_x_marche ";
		try{
			return jdbcTemplate.queryForObject(queryStr, Integer.class);

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return 0;
		}
	}
	
	/**
	 * @param marca
	 * @return String
	 */
	@Transactional()
	public String saveOrUpdateMarca(Marca marca) throws SQLIntegrityConstraintViolationException {
		String idMarca = "";

		if (marca.getIdMarca() == null) {
			idMarca = save(marca);
			logger.info("Inserita marca: " + idMarca + " " + marca.getDescrMarca() );
		} else {
			idMarca = update(marca);
		}
		
		return idMarca;
	}
	
	/**
	 * @param marca
	 * @return idTipologia
	 */
	@Transactional()
	private String save(Marca marca) {

		String query = "INSERT INTO mg_x_marche "
				+ "(descr_marca) "
				+ "VALUES (upper(:descrMarca))";

		KeyHolder keyHolder = new GeneratedKeyHolder();
		
		SqlParameterSource parameters = new BeanPropertySqlParameterSource(marca);
		NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters, keyHolder);

		return String.valueOf(keyHolder.getKey());
	}
	
	/**
	 * @param marca
	 * @return idTipologia
	 */
	@Transactional()
	private String update(Marca marca) {
		String query = "UPDATE mg_x_marche "
				+ "SET "
				+ "descr_marca = upper(:descrMarca) "
				+ "WHERE id_marca = :idMarca";

		SqlParameterSource parameters = new BeanPropertySqlParameterSource(marca);
		NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters);
		
		return marca.getIdMarca();
	}
	
	/**
	 * @param marca
	 * @return
	 */
	@Transactional()
	public String delete(Marca marca) {
		String ret = "";
		String query = "SELECT count(*) FROM mg_prodotti where id_marca = ?";
		int count = jdbcTemplate.queryForObject(query, Integer.class, new Object[] {marca.getIdMarca()});
		ret = Def.STR_ERROR_501;
		if(count == 0) {
			query = "DELETE from mg_x_marche WHERE id_marca = :idMarca";
	
			SqlParameterSource parameters = new BeanPropertySqlParameterSource(marca);
			NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
			namedParameterJdbcTemplate.update(query, parameters);
			ret = Def.STR_OK;
		}
		return ret;
	}
	
	/************************************* TIPOLOGIA **********************************************************/
	/**
	 * @param ricerca
	 * @return List<Tipologia>
	 */
	@Transactional(readOnly = true)
	public List<Tipologia> getTipologie(RicercaDTO ricerca) {
		String where = "";
		String forPagination = "";
		String queryStr = "";
		
		if(ricerca.getLimit() != null && ricerca.getOffset() != null && !"".equals(ricerca.getLimit()) && !"".equals(ricerca.getOffset())) {
			forPagination = "limit " + ricerca.getLimit() + "," + ricerca.getOffset();
		}
		
		if(!"".equalsIgnoreCase(ricerca.getIdMagazzino())) {
			where = " where pm.id_magazzino = " + ricerca.getIdMagazzino() + " ";
			queryStr = "Select "
					+ "    t.id_tipologia, "
					+ "    t.descr_tipologia, "
					+ "    Count(p.id_prodotto) num_prodotti, "
					+ "    t.colore, "
					+ "    t.immagine "
					+ "From "
					+ "    mg_x_tipologia t Left Join "
					+ "    mg_prodotti p On p.id_tipologia = t.id_tipologia Left Join "
					+ "    mg_r_prodotto_magazzino pm On pm.id_prodotto = p.id_prodotto "
					+ where
					+ "Group By "
					+ "    t.id_tipologia, "
					+ "    t.descr_tipologia, "
					+ "    t.colore, "
					+ "    t.immagine, "
					+ "    pm.id_magazzino " + forPagination;
		}else {
			queryStr = "Select "
					+ "    t.id_tipologia, "
					+ "    t.descr_tipologia, "
					+ "    Count(p.id_prodotto) num_prodotti, "
					+ "    t.colore, "
					+ "    t.immagine "
					+ "From "
					+ "    mg_x_tipologia t Left Join "
					+ "    mg_prodotti p On p.id_tipologia = t.id_tipologia "
					+ "Group By "
					+ "    t.id_tipologia, "
					+ "    t.descr_tipologia, "
					+ "    t.colore, "
					+ "    t.immagine " + forPagination;
		}
		try{
			return jdbcTemplate.query(queryStr, new TipologiaRowMapper());

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	/**
	 * @param 
	 * @return int
	 */
	@Transactional(readOnly = true)
	public int getCountTipologie() {
		String queryStr = "Select count(*) From mg_x_tipologia" ;
		try{
			return jdbcTemplate.queryForObject(queryStr, Integer.class);

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return 0;
		}
	}
	
	/**
	 * @param idTipologia
	 * @return List<Tipologia>
	 */
	@Transactional(readOnly = true)
	public Tipologia getTipologiaByID(String idTipologia) {

		String queryStr = "Select "
				+ "    t.id_tipologia, "
				+ "    t.descr_tipologia, "
				+ "    Count(p.id_prodotto) num_prodotti, "
				+ "    t.colore, "
				+ "    t.immagine "
				+ " From "
				+ "    mg_x_tipologia t Left Join "
				+ "    mg_prodotti p On p.id_tipologia = t.id_tipologia Left Join "
				+ "    mg_r_prodotto_magazzino pm On pm.id_prodotto = p.id_prodotto "
				+ " Where t.id_tipologia = ?"
				+ " Group By "
				+ "    t.id_tipologia, "
				+ "    t.descr_tipologia, "
				+ "    t.colore, "
				+ "    t.immagine ";
		try{
			return jdbcTemplate.queryForObject(queryStr, new TipologiaRowMapper(),new Object[] {idTipologia});

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	/**
	 * @param tipologia
	 * @return String
	 */
	@Transactional()
	public String saveOrUpdateTipologia(Tipologia tipologia) throws SQLIntegrityConstraintViolationException {
		String idTipologia = "";

		if (tipologia.getIdTipologia() == null) {
			idTipologia = save(tipologia);
			logger.info("Inserita tipologia: " + idTipologia + " " + tipologia.getDescrTipologia() );
		} else {
			idTipologia = update(tipologia);
		}
		
		return idTipologia;
	}
	
	/**
	 * @param tipologia
	 * @return idTipologia
	 */
	@Transactional()
	private String save(Tipologia tipologia) {

		String query = "INSERT INTO mg_x_tipologia "
				+ "(descr_tipologia,colore,immagine) "
				+ "VALUES (upper(:descrTipologia),:colore,:immagine)";

		KeyHolder keyHolder = new GeneratedKeyHolder();
		
		SqlParameterSource parameters = new BeanPropertySqlParameterSource(tipologia);
		NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters, keyHolder);

		return String.valueOf(keyHolder.getKey());
	}
	
	/**
	 * @param tipologia
	 * @return idTipologia
	 */
	@Transactional()
	private String update(Tipologia tipologia) {
		String query = "UPDATE mg_x_tipologia "
				+ "SET "
				+ "descr_tipologia = upper(:descrTipologia), "
				+ "colore = :colore, "
				+ "immagine = :immagine "
				+ "WHERE id_tipologia = :idTipologia";

		SqlParameterSource parameters = new BeanPropertySqlParameterSource(tipologia);
		NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters);
		
		return tipologia.getIdTipologia();
	}
	
	/**
	 * @param tipologia
	 * @return
	 */
	@Transactional()
	public String delete(Tipologia tipologia) {
		String ret = "";
		String query = "SELECT count(*) FROM mg_prodotti where id_tipologia = ?";
		int count = jdbcTemplate.queryForObject(query, Integer.class, new Object[] {tipologia.getIdTipologia()});
		ret = Def.STR_ERROR_501;
		if(count == 0) {
			query = "DELETE from mg_x_tipologia WHERE id_tipologia = :idTipologia";
	
			SqlParameterSource parameters = new BeanPropertySqlParameterSource(tipologia);
			NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
			namedParameterJdbcTemplate.update(query, parameters);
			ret = Def.STR_OK;
		}
		return ret;
	}
	
	/************************************* SOTTOTIPOLOGIA **********************************************************/
	/**
	 * @param ricerca
	 * @return List<SottoTipologia>
	 */
	@Transactional(readOnly = true)
	public List<SottoTipologia> getSottoTipologie(RicercaDTO ricerca) {
		String where = "";
		String forPagination = "";
		
		if(ricerca.getLimit() != null && ricerca.getOffset() != null && !"".equals(ricerca.getLimit()) && !"".equals(ricerca.getOffset())) {
			forPagination = "limit " + ricerca.getLimit() + "," + ricerca.getOffset();
		}
		
		if(!"".equalsIgnoreCase(ricerca.getIdTipologia())) {
			where = " where (st.id_tipologia = " + ricerca.getIdTipologia() + " or st.id_tipologia is null) ";
		}
		String queryStr = "Select"
				+ "    st.id_sotto_tipologia,"
				+ "    st.id_tipologia,"
				+ "    st.descr_sotto_tipologia,"
				+ "    st.colore,"
				+ "    st.immagine,"
				+ "    Count(p.id_prodotto) num_prodotti "
				+ " From "
				+ "    mg_prodotti p Right Join"
				+ "    mg_x_sotto_tipologia st On p.id_sotto_tipologia = st.id_sotto_tipologia "
				+ where
				+ " Group By st.id_sotto_tipologia,"
				+ "    st.id_tipologia,"
				+ "    st.descr_sotto_tipologia,"
				+ "    st.colore,"
				+ "    st.immagine " + forPagination;
		try{
			return jdbcTemplate.query(queryStr, new SottoTipologiaRowMapper());

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	/**
	 * @param 
	 * @return int
	 */
	@Transactional(readOnly = true)
	public int getCountSottoTipologie() {
		String queryStr = "Select count(*) From mg_x_sotto_tipologia" ;
		try{
			return jdbcTemplate.queryForObject(queryStr, Integer.class);

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return 0;
		}
	}
	
	/**
	 * @param idSottoTipologia
	 * @return List<SottoTipologia>
	 */
	@Transactional(readOnly = true)
	public SottoTipologia getSottoTipologiaByID(String idSottoTipologia) {

		String queryStr = "Select"
				+ "    st.id_sotto_tipologia,"
				+ "    st.id_tipologia,"
				+ "    st.descr_sotto_tipologia,"
				+ "    st.colore,"
				+ "    st.immagine,"
				+ "    Count(p.id_prodotto) num_prodotti "
				+ " From "
				+ "    mg_prodotti p Inner Join"
				+ "    mg_x_sotto_tipologia st On p.id_sotto_tipologia = st.id_sotto_tipologia "
				+ " Where st.id_sotto_tipologia = ? "
				+ " Group By st.id_sotto_tipologia,"
				+ "    st.id_tipologia,"
				+ "    st.descr_sotto_tipologia,"
				+ "    st.colore,"
				+ "    st.immagine ";
		try{
			return jdbcTemplate.queryForObject(queryStr, new SottoTipologiaRowMapper(),new Object[] {idSottoTipologia});

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	/**
	 * @param idTipologia
	 * @return List<SottoTipologia>
	 */
	@Transactional(readOnly = true)
	public List<SottoTipologia> getSottoTipologiaByIDTipologia(String idTipologia) {

		String queryStr = "Select"
				+ "    st.id_sotto_tipologia,"
				+ "    st.id_tipologia,"
				+ "    st.descr_sotto_tipologia,"
				+ "    st.colore,"
				+ "    st.immagine,"
				+ "    Count(p.id_prodotto) num_prodotti "
				+ " From "
				+ "    mg_prodotti p Right Join"
				+ "    mg_x_sotto_tipologia st On p.id_sotto_tipologia = st.id_sotto_tipologia "
				+ " Where st.id_tipologia = ? "
				+ " Group By st.id_sotto_tipologia,"
				+ "    st.id_tipologia,"
				+ "    st.descr_sotto_tipologia,"
				+ "    st.colore,"
				+ "    st.immagine ";
		try{
			return jdbcTemplate.query(queryStr, new SottoTipologiaRowMapper(),new Object[] {idTipologia});

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	/**
	 * @param sottoTipologia
	 * @return String
	 */
	@Transactional()
	public String saveOrUpdateSottoTipologia(SottoTipologia sottoTipologia) throws SQLIntegrityConstraintViolationException {
		String idSottoTipologia = "";

		if (sottoTipologia.getIdSottoTipologia()== null) {
			idSottoTipologia = save(sottoTipologia);
			logger.info("Inserita sotto tipologia: " + idSottoTipologia + " " + sottoTipologia.getIdSottoTipologia() );
		} else {
			idSottoTipologia = update(sottoTipologia);
		}
		
		return idSottoTipologia;
	}
	
	/**
	 * @param sottoTipologia
	 * @return idSottoTipologia
	 */
	@Transactional()
	private String save(SottoTipologia sottoTipologia) {

		String query = "INSERT INTO mg_x_sotto_tipologia "
				+ "(id_tipologia, descr_sotto_tipologia,colore,immagine) "
				+ "VALUES (:idTipologia,upper(:descrSottoTipologia),:colore,:immagine)";

		KeyHolder keyHolder = new GeneratedKeyHolder();
		
		SqlParameterSource parameters = new BeanPropertySqlParameterSource(sottoTipologia);
		NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters, keyHolder);

		return String.valueOf(keyHolder.getKey());
	}
	
	/**
	 * @param sottoTipologia
	 * @return idSottoTipologia
	 */
	@Transactional()
	private String update(SottoTipologia sottoTipologia) {
		String query = "UPDATE mg_x_sotto_tipologia "
				+ "SET "
				+ "id_tipologia = upper(:idTipologia), "
				+ "descr_sotto_tipologia = upper(:descrSottoTipologia), "
				+ "colore = :colore, "
				+ "immagine = :immagine "
				+ "WHERE id_sotto_tipologia = :idSottoTipologia";

		SqlParameterSource parameters = new BeanPropertySqlParameterSource(sottoTipologia);
		NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters);
		
		return sottoTipologia.getIdSottoTipologia();
	}
	
	/**
	 * @param sottoTipologia
	 * @return
	 */
	@Transactional()
	public String delete(SottoTipologia sottoTipologia) {
		String ret = "";
		String query = "SELECT count(*) FROM mg_prodotti where id_sotto_tipologia = ?";
		int count = jdbcTemplate.queryForObject(query, Integer.class, new Object[] {sottoTipologia.getIdSottoTipologia()});
		ret = Def.STR_ERROR_501;
		if(count == 0) {
			query = "DELETE from mg_x_sotto_tipologia WHERE id_sotto_tipologia = :idSottoTipologia";
	
			SqlParameterSource parameters = new BeanPropertySqlParameterSource(sottoTipologia);
			NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
			namedParameterJdbcTemplate.update(query, parameters);
			ret = Def.STR_OK;
		}
		return ret;
	}
	/***********************************************************************************************************/
	private static class MagazzinoRowMapper extends BaseRowMapper<Magazzino> {
		public MagazzinoRowMapper() {}		
		@Override
		public Magazzino mapRowImpl(ResultSet rs, int i) throws SQLException {
		
			Magazzino o = new Magazzino();
			o.setIdMagazzino(rs.getString("id_magazzino"));
			o.setDescrMagazzino(rs.getString("descr_magazzino"));
			return o;
		}
	}
	
	private static class ColoreRowMapper extends BaseRowMapper<Colore> {
		public ColoreRowMapper() {}		
		@Override
		public Colore mapRowImpl(ResultSet rs, int i) throws SQLException {
		
			Colore o = new Colore();
			o.setIdColore(rs.getString("id_colore"));
			o.setDescrColore(rs.getString("descr_colore"));
			return o;
		}
	}
	
	private static class TagliaRowMapper extends BaseRowMapper<Taglia> {
		public TagliaRowMapper() {}		
		@Override
		public Taglia mapRowImpl(ResultSet rs, int i) throws SQLException {
		
			Taglia o = new Taglia();
			o.setIdTaglia(rs.getString("id_taglia"));
			o.setDescrTaglia(rs.getString("descr_taglia"));
			return o;
		}
	}
	
	private static class MarcaRowMapper extends BaseRowMapper<Marca> {
		public MarcaRowMapper() {}		
		@Override
		public Marca mapRowImpl(ResultSet rs, int i) throws SQLException {
		
			Marca o = new Marca();
			o.setIdMarca(rs.getString("id_marca"));
			o.setDescrMarca(rs.getString("descr_marca"));
			return o;
		}
	}
	
	private static class ProdottoRowMapper extends BaseRowMapper<Prodotto> {
		public ProdottoRowMapper() {}		
		@Override
		public Prodotto mapRowImpl(ResultSet rs, int i) throws SQLException {
		
			Prodotto o = new Prodotto();
			o.setIdProdotto(rs.getString("id_prodotto"));
			o.setDescrProdotto(rs.getString("descr_prodotto"));
			o.setIdTipologia(rs.getString("id_tipologia"));
			o.setIdMarca(rs.getString("id_marca"));
			o.setPrezzo(rs.getString("prezzo"));
			o.setImmagine(rs.getString("immagine"));
			o.setAccount(rs.getString("account"));
			o.setDtAggiornamento(rs.getString("dt_aggiornamento"));
			o.setNumPezzi(rs.getString("numPezzi"));
			o.setIdSottoTipologia(rs.getString("id_sotto_tipologia"));
			return o;
		}
	}
	
	private static class MagazzinoProdottoRowMapper extends BaseRowMapper<MagazzinoProdotto> {
		public MagazzinoProdottoRowMapper() {}		
		@Override
		public MagazzinoProdotto mapRowImpl(ResultSet rs, int i) throws SQLException {
		
			MagazzinoProdotto o = new MagazzinoProdotto();
			o.setIdProdotto(rs.getString("id_prodotto"));
			o.setId(rs.getString("id"));
			o.setIdColore(rs.getString("id_colore"));
			o.setIdMagazzino(rs.getString("id_magazzino"));
			o.setIdTaglia(rs.getString("id_taglia"));
			o.setQuantita(rs.getString("quantita"));
			return o;
		}
	}
	
	private static class TipologiaRowMapper extends BaseRowMapper<Tipologia> {
		public TipologiaRowMapper() {}		
		@Override
		public Tipologia mapRowImpl(ResultSet rs, int i) throws SQLException {
		
			Tipologia o = new Tipologia();
			o.setIdTipologia(rs.getString("id_tipologia"));
			o.setDescrTipologia(rs.getString("descr_tipologia"));
			o.setNumProdotti(rs.getString("num_prodotti"));
			o.setColore(rs.getString("colore"));
			o.setImmagine(rs.getString("immagine"));
			return o;
		}
	}
	
	private static class SottoTipologiaRowMapper extends BaseRowMapper<SottoTipologia> {
		public SottoTipologiaRowMapper() {}		
		@Override
		public SottoTipologia mapRowImpl(ResultSet rs, int i) throws SQLException {
		
			SottoTipologia o = new SottoTipologia();
			o.setIdSottoTipologia(rs.getString("id_sotto_tipologia"));
			o.setIdTipologia(rs.getString("id_tipologia"));
			o.setDescrSottoTipologia(rs.getString("descr_sotto_tipologia"));
			o.setNumProdotti(rs.getString("num_prodotti"));
			o.setColore(rs.getString("colore"));
			o.setImmagine(rs.getString("immagine"));
			return o;
		}
	}

}
