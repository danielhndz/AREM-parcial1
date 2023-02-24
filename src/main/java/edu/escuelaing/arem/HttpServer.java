package edu.escuelaing.arem;

import java.net.*;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class HttpServer {
	public static void main(String[] args) throws IOException {
		ServerSocket serverSocket = null;
		Socket clientSocket = null;
		serverSocket = new ServerSocket(35000);
		while (true) {
			try {
				clientSocket = serverSocket.accept();
				System.out.println("Client socket accepted ...");
				listenClientSocket(clientSocket);
			} catch (IOException e) {
				System.err.println("Accept failed.");
				// System.exit(1);
				clientSocket.close();
				serverSocket.close();
			}
		}
	}

	private static void listenClientSocket(Socket clientSocket) throws IOException {
		PrintWriter out = new PrintWriter(
				clientSocket.getOutputStream(), true);
		BufferedReader in = new BufferedReader(
				new InputStreamReader(clientSocket.getInputStream()));
		String inputLine, header, outputLine, loadGetMsg, loadPostMsg;
		StringBuilder inputLines = new StringBuilder();
		header = "HTTP/1.1 200 OK\r\n"
				+ "Content-Type: text/html\r\n\r\n";
		loadGetMsg = "function loadGetMsg() {" +
				"\n\tlet command = document.getElementById('command').value;" +
				"\n\tconst xhttp = new XMLHttpRequest();" +
				"\n\txhttp.onload = function() {" +
				"\n\t\tdocument.getElementById('getrespmsg').innerHTML =" +
				"\n\t\tthis.responseText;" +
				"\n\t}" +
				"\n\txhttp.open('GET', 'localhost/consulta?command='+command);" +
				"\n\txhttp.send();" +
				"\n}";
		loadPostMsg = "function loadPostMsg(command){" +
				"\n\tlet url = 'localhost/consulta?command=' + command.value;" +
				"\n\tfetch (url, {method: 'POST'})" +
				"\n\t\t.then(x => x.text())" +
				"\n\t\t.then(y => document.getElementById('postrespmsg').innerHTML = y);" +
				"\n}";
		outputLine = "<!DOCTYPE html>" +
				"<html>" +
				"<head>" +
				"<meta charset='UTF-8'>" +
				"<title>AREM</title>\n" +
				"</head>" +
				"<body>" +
				"<h1>Get form</h1>" +
				"<form method='dialog'>" +
				"<label for='command'>Command:</label><br>" +
				"<input type='text' id='command' name='command' placeholder='Write a valid command'><br><br>" +
				"<input type='button' value='Submit' onclick='loadGetMsg()'>" +
				"</form>" +
				"<div id='getrespmsg'></div>" +
				"<script>" +
				loadGetMsg +
				"</script>" +
				"<h1>Post form</h1>" +
				"<form method='dialog'>" +
				"<label for='postcommand'>Command:</label><br>" +
				"<input type='text' id='postcommand' name='postcommand' placeholder='Write a valid command'><br><br>" +
				"<input type='button' value='Submit' onclick='loadPostMsg(postcommand)'>" +
				"</form>" +
				"<div id='postrespmsg'></div>" +
				"<script>" +
				loadPostMsg +
				"</script>" +
				"</body>" +
				"</html>";
		boolean firstLine = true;
		boolean consulta = false;
		while ((inputLine = in.readLine()) != null) {
			inputLines.append(inputLine + "\n");
			if (firstLine) {
				String[] data = inputLine.split(" ");
				String path = data[1];
				firstLine = false;
				if (path.contains("/localhost/consulta?command=")) {
					consulta = true;
					String command = path.replace("/localhost/consulta?command=", "");
					consulta(out, command);
				}
			}
			if (!in.ready()) {
				break;
			}
		}
		System.out.println("\n\nRecibí: " + inputLines + "\n\n");
		if (!consulta) {
			out.println(header + outputLine);
		}
		out.close();
		in.close();
	}

	private static void consulta(PrintWriter out, String command) {
		System.out.println("\n\n\tRecibiendo comando = " + command);
		if (command.startsWith("class")) {
			String className = command
					.replace("class", "")
					.replace("(", "")
					.replace(")", "");
			out.println(classCommand(className));
		} else if (command.startsWith("invoke")) {
			String[] args = command
					.replace("invoke", "")
					.replace("(", "")
					.replace(")", "")
					.split(",");
			out.println(invokeCommand(args[0], args[1]));
		} else {
			out.println("echo :: " + command);
		}
	}

	private static String classCommand(String className) {
		StringBuilder builder = new StringBuilder();
		builder.append("HTTP/1.1 200 OK\r\n");
		builder.append("Content-Type: text/html\r\n\r\n");
		builder.append("<!DOCTYPE html>");
		builder.append("<html>");
		builder.append("<head>");
		builder.append("<meta charset='UTF-8'>");
		builder.append("<title>AREM</title>\n");
		builder.append("</head>");
		builder.append("<body>");
		Class<?> targetClass;
		try {
			targetClass = Class.forName(className);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			builder.append("Error");
			builder.append("</body>");
			return builder.toString();
		}
		builder.append("<h4>Campos declarados</h4>");
		for (Field field : targetClass.getDeclaredFields()) {
			builder.append(field + "<br/>");
		}
		builder.append("<h4>Metodos declarados</h4>");
		for (Method method : targetClass.getDeclaredMethods()) {
			builder.append(method + "<br/>");
		}
		builder.append("</body>");

		return builder.toString();
	}

	private static String invokeCommand(String className, String methodName) {
		StringBuilder builder = new StringBuilder();
		builder.append("HTTP/1.1 200 OK\r\n");
		builder.append("Content-Type: text/html\r\n\r\n");
		builder.append("<!DOCTYPE html>");
		builder.append("<html>");
		builder.append("<head>");
		builder.append("<meta charset='UTF-8'>");
		builder.append("<title>AREM</title>\n");
		builder.append("</head>");
		builder.append("<body>");
		Class<?> targetClass;
		try {
			targetClass = Class.forName(className);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			builder.append("Error");
			builder.append("</body>");
			return builder.toString();
		}
		for (Method method : targetClass.getDeclaredMethods()) {
			if (method.getName() == methodName) {
				try {
					builder.append(method.invoke(targetClass));
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		builder.append("</body>");

		return builder.toString();
	}
}