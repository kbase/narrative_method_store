package us.kbase.narrativemethodstore;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public class ImageServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;

	public static void main(String[] args) throws Exception {
		int port = 8888;
		if (args.length == 1)
			port = Integer.parseInt(args[0]);
		Server jettyServer = new Server(port);
		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setContextPath("/");
		jettyServer.setHandler(context);
		context.addServlet(new ServletHolder(new ImageServlet()), "/img");
		jettyServer.start();
		jettyServer.join();
	}
	
	@Override
	protected void doOptions(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		setupResponseHeaders(request, response);
		response.setContentLength(0);
		response.getOutputStream().print("");
		response.getOutputStream().flush();
	}

	private static void setupResponseHeaders(HttpServletRequest request,
			HttpServletResponse response) {
		response.setHeader("Access-Control-Allow-Origin", "*");
		String allowedHeaders = request.getHeader("HTTP_ACCESS_CONTROL_REQUEST_HEADERS");
		response.setHeader("Access-Control-Allow-Headers", allowedHeaders == null ? "authorization" : allowedHeaders);
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)  
            throws IOException { 
		String methodId = request.getParameter("method_id");
		String appId = request.getParameter("app_id");
		String typeName = request.getParameter("type_name");
		int defined = 0;
		if (methodId != null)
			defined++;
		if (appId != null)
			defined++;
		if (typeName != null)
			defined++;
		if (defined < 1) 
			throw new IllegalStateException("Neither of parameters method_id / app_id / type_name is defined");
		if (defined > 1)
			throw new IllegalStateException("Only one of parameters method_id / app_id / type_name should be defined");			
		if (methodId != null && (methodId.contains("../") || methodId.trim().isEmpty()))
			throw new IllegalStateException("Parameter method_id is wrong: " + methodId);
		if (appId != null && (appId.contains("../") || appId.trim().isEmpty()))
			throw new IllegalStateException("Parameter app_id is wrong: " + appId);
		if (typeName != null && (typeName.contains("../") || typeName.trim().isEmpty()))
			throw new IllegalStateException("Parameter type_name is wrong: " + appId);
		String imageName = request.getParameter("image_name");
		if (imageName == null || imageName.contains("../") || imageName.trim().isEmpty())
			throw new IllegalStateException("Parameter image_name is wrong");
		String imageExt = imageName.contains(".") ? imageName.substring(imageName.indexOf('.') + 1).toLowerCase() : "png";
		String path = null;
		try {
			path = NarrativeMethodStoreServer.config().get(NarrativeMethodStoreServer.CFG_PROP_GIT_LOCAL_DIR);
		} catch (Exception ex) {
			System.err.println(ex.getMessage());
		}
		if (path == null)
    		path = "../narrative_method_specs";
		File innerDir;
		if (methodId != null) {
			innerDir = new File(new File(path, "methods"), methodId);
		} else if (appId != null) {
			innerDir = new File(new File(path, "apps"), appId);
		} else {
			innerDir = new File(new File(path, "types"), typeName);
		}
    	File imageFile = new File(new File(innerDir, "img"), imageName);
		setupResponseHeaders(request, response);
		response.setContentType("image/" + imageExt);
		OutputStream os = response.getOutputStream();
		InputStream is = new FileInputStream(imageFile);
		IOUtils.copy(is, os);
	}
}
