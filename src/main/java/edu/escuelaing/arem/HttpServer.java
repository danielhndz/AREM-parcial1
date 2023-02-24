package edu.escuelaing.arem;

import java.net.*;
import java.io.*;
import java.lang.reflect.Field;
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
		String header = "HTTP/1.1 200 OK\r\n"
				+ "Content-Type: text/plain\r\n\r\n";
		if (command.startsWith("class")) {
			String className = command
					.replace("class", "")
					.replace("(", "")
					.replace(")", "");
			out.println(header + classCommand(className));
		}
	}

	private static String classCommand(String className) {
		StringBuilder response = new StringBuilder();
		Class<?> targetClass;
		try {
			targetClass = Class.forName(className);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		}
		response.append("Campos declarados");
		for (Field field : targetClass.getDeclaredFields()) {
			response.append("\n\t" + field);
		}
		response.append("Métodos declarados");
		for (Method method : targetClass.getDeclaredMethods()) {
			response.append("\n\t" + method);
		}
		return response.toString();
	}
}