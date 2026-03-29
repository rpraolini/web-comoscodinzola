package it.asso.core.controller.report;

import net.sf.jasperreports.engine.export.HtmlExporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager; // Per exportPdf
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter; // Per exportXlsx
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleWriterExporterOutput;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;
import net.sf.jasperreports.export.SimpleHtmlExporterOutput;

import javax.management.JMRuntimeException;
import java.io.IOException;



@Component
public class SimpleReportExporter {

    private static Logger logger = LoggerFactory.getLogger(SimpleReportExporter.class); // Usa la classe corretta

    private JasperPrint jasperPrint;

    public SimpleReportExporter() {
    }

    public SimpleReportExporter(JasperPrint jasperPrint) {
        this.jasperPrint = jasperPrint;
    }

    public JasperPrint getJasperPrint() {
        return jasperPrint;
    }

    public void setJasperPrint(JasperPrint jasperPrint) {
        this.jasperPrint = jasperPrint;
    }


    public void exportPdf(HttpServletResponse response, String filename) throws IOException, JMRuntimeException, JRException {

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition","inline; filename=" + filename + ".pdf");

        // Utilizza il metodo statico per l'export PDF
        JasperExportManager.exportReportToPdfStream(jasperPrint, response.getOutputStream());

        response.getOutputStream().flush();
        response.getOutputStream().close();
    }

    public void exportXlsx(HttpServletResponse response, String sheetName) throws IOException, JMRuntimeException, JRException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"); // MIME corretto per XLSX
        response.setHeader("Content-Disposition","inline; filename=" + sheetName + ".xlsx"); // Utilizza sheetName

        SimpleXlsxReportConfiguration configuration = new SimpleXlsxReportConfiguration();
        configuration.setRemoveEmptySpaceBetweenRows(true);
        configuration.setRemoveEmptySpaceBetweenColumns(true);

        JRXlsxExporter exporterXLS = new JRXlsxExporter();
        exporterXLS.setExporterInput(new SimpleExporterInput(jasperPrint));
        exporterXLS.setExporterOutput(new SimpleOutputStreamExporterOutput(response.getOutputStream()));

        exporterXLS.setConfiguration(configuration);
        exporterXLS.exportReport(); // Lancia JRException

        response.getOutputStream().flush();
        response.getOutputStream().close();
    }

    public void exportToCsv(String fileName) throws JRException {
        JRCsvExporter exporter = new JRCsvExporter();

        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        exporter.setExporterOutput(new SimpleWriterExporterOutput(fileName));

        exporter.exportReport(); // Lancia JRException
    }

    public void exportToHtml(String fileName) throws JRException {
        HtmlExporter exporter = new HtmlExporter();

        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        exporter.setExporterOutput(new SimpleHtmlExporterOutput(fileName));

        exporter.exportReport(); // Lancia JRException
    }
}