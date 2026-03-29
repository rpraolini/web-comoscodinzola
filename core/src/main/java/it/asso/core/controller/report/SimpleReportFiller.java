package it.asso.core.controller.report;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.util.JRSaver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;


@Component
public class SimpleReportFiller {

	private static Logger logger = LoggerFactory.getLogger(SimpleReportFiller.class.getName());
	
    private String reportFileName;

    private JasperReport jasperReport;

    private JasperPrint jasperPrint;

    @Autowired private DataSource dataSource;

    private Map<String, Object> parameters;

    public SimpleReportFiller() {
        parameters = new HashMap<>();
    }

    public void prepareReport() throws FileNotFoundException, SQLException {
        compileReport();
        fillReport();
    }

    public void compileReport() throws FileNotFoundException {
        try {
        	boolean toCompile = true;
        	Path jasper = Paths.get(reportFileName.replace(".jrxml", ".jasper"));
            boolean jasperExist = Files.exists(jasper);
            if(jasperExist) {
            	File fileJasper = new File(reportFileName.replace(".jrxml", ".jasper"));
            	File fileJrxml = new File(reportFileName);
            	
            	if(fileJasper.lastModified() < fileJrxml.lastModified()){
            		toCompile = true;
            	}else {
            		toCompile = false;
            	}           	
            }else {
            	toCompile = true;
            }
            
           if(toCompile) {
	        	File file = new File(reportFileName);
	            InputStream reportStream = new FileInputStream(file);
	            jasperReport = JasperCompileManager.compileReport(reportStream);
	            JRSaver.saveObject(jasperReport, reportFileName.replace(".jrxml", ".jasper"));
           }else {
        	   jasperReport = (JasperReport) JRLoader.loadObject(new File(reportFileName.replace(".jrxml", ".jasper")));
           }
        } catch (JRException ex) {
        	logger.error(ex.getMessage());
        }
    }

    public void fillReport() throws SQLException {
    	jasperPrint = null;
    	Connection conn = null;
        try {
        	conn = dataSource.getConnection();
            jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, conn);
        } catch (JRException | SQLException ex) {
        	logger.error(ex.getMessage());
        }finally {
        	if(conn != null) {conn.close();}
        }
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    public String getReportFileName() {
        return reportFileName;
    }

    public void setReportFileName(String reportFileName) {
        this.reportFileName = reportFileName;
    }

    public JasperPrint getJasperPrint() {
        return jasperPrint;
    }

}
