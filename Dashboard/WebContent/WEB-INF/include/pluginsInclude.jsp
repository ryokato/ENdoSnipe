<%@page import="java.io.FileReader"%>
<%@page import="java.io.FileInputStream"%>
<%@page import="java.io.BufferedReader"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ page import="java.io.File"%>
<%@ page import="java.lang.String"%>
<%@ page import="java.util.ArrayList"%>
<%@ page import="java.util.List"%>
<%@ page import="org.wgp.file.filter.FileNameFilter"%>
<%@ page import="org.wgp.file.util.FilePathUtil"%>

<%
	// プラグイン単位のディレクトリ名を取得する
	String pluginsDirectoryPath = config.getServletContext()
			.getRealPath("/resources/plugins");
	File pluginsDirectory = new File(pluginsDirectoryPath);
	String[] pluginNames = pluginsDirectory.list();

	// プラグインがない場合、インクルード処理を行わず終了する。
	if (pluginNames != null) {
		/*
		 * 各プラグインディレクトリ直下にあるconfig.txtファイルからインポートするjsファイル/cssファイルを取得し、
		 * jsファイル/cssファイルそれぞれに対してインポートタグを生成する。
		 */
		for (String pluginName : pluginNames) {
			File elementDirectory = new File(pluginsDirectory,
					pluginName);
			File configFile = new File(elementDirectory, "include.txt");
			// ファイルが存在しない場合はそのプラグインのファイルインポートはしない
			boolean isExist = configFile.exists();
			if (!isExist) {
				System.out
						.println(elementDirectory
								+ "\\include.txtが存在しないため、このプラグインファイルの読込はできませんでした。");
				continue;
			}

			BufferedReader reader = new BufferedReader(new FileReader(
					configFile));
			String line;

			while ((line = reader.readLine()) != null) {
				String tmpfilePath = "/resources/plugins/" + pluginName
						+ "/" + line;
				String filePath = tmpfilePath.replaceAll("\\\\", "/");
				if (line.endsWith("js")) {
					out.print("<script type=\"text/javascript\" ");
					out.print("src=\"" + request.getContextPath()
							+ filePath + "\">");
					out.println("</script>");
				} else if (line.endsWith("css")) {
					out.print("<link rel=\"stylesheet\" ");
					out.print("href=\"" + request.getContextPath()
							+ filePath + "\" ");
					out.print("type=\"text/css\" ");
					out.print("media=\"all\">");
				}
			}
			reader.close();
		}
	}
%>