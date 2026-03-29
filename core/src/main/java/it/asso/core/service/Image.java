package it.asso.core.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;


import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;import org.slf4j.LoggerFactory;


public class Image extends HttpServlet {

	private static final long serialVersionUID = 9116916705898578538L;

	private static Logger logger = LoggerFactory.getLogger(Image.class);
	
	private String temp_path ;
	

    public Image() {
        super();
    }
    
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
	        Properties prop = new Properties();
	        try {
	            prop.load(Image.class.getClassLoader().getResourceAsStream("app.properties"));  
	            temp_path = prop.getProperty("path_doc");
	        } catch (IOException ex) {
	            logger.error(ex.getMessage());
	        }
	        
        }
    

		public void doGet(HttpServletRequest req, HttpServletResponse resp) throws  ServletException, IOException {

			try {
		      ServletContext cntx= req.getSession().getServletContext();

		      String filename = temp_path + req.getParameter("id") + "//" +  req.getParameter("nome");
		      String mime = cntx.getMimeType(filename);

		      if (mime == null) {
		        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		        return;
		      }

		      resp.setContentType(mime);
		      File file = new File(filename);
		      if (file.exists()) {
			      resp.setContentLength((int)file.length());
	
			      FileInputStream in = new FileInputStream(file);
			      OutputStream  out = resp.getOutputStream();
	
			       byte[] buf = new byte[1024];
			       int count = 0;
			       while ((count = in.read(buf)) >= 0) {
			         out.write(buf, 0, count);
			      }
			      out.close();
			      in.close();
		      }
			}catch(Exception ex) {
				logger.error(ex.getMessage());
			}

		}


}
