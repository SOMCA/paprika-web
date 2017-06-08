package spoon.main;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONObject;

public class GitpullRequest {

	private String nameUser;
	private String nameProject;
	private String branch;

	public GitpullRequest(String nameUser, String nameProject, String branch) {
		this.nameProject = nameProject; // spoon-test
		this.nameUser = nameUser; // Snrasha
		this.branch = branch; // de
	}

	public void getData() throws IllegalStateException, IOException {
		String url = "https://api.github.com/repos/" + App.nameUser + "/" + this.nameProject + "/pulls";
		HttpClient client = HttpClientBuilder.create().build();
		HttpPost post = new HttpPost(url);

		InputStream is = new FileInputStream("./info.json");
		String jsonTxt = IOUtils.toString(is);
		System.out.println(jsonTxt);
		JSONObject json = new JSONObject(jsonTxt);

		post.addHeader("Authorization","token "+ json.getString("token"));
		
		
		StringEntity params = new StringEntity(
				"{ \"title\": \"Amazing new feature\"," + "\"body\": \"Please pull this in!\"," + "\"head\": \""
						+ App.nameBot + ":" + this.branch + "\"," + "\"base\": \"" + "master" + "\" }",
				ContentType.APPLICATION_JSON);
		post.setEntity(params);
		
		HttpResponse response = client.execute(post);
		System.out.println("Response Code : " + response.getStatusLine().getStatusCode());
		System.out.flush();
		BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

		StringBuffer result = new StringBuffer();
		String line = "";
		while ((line = rd.readLine()) != null) {
			result.append(line);
		}
		System.out.println(result.toString());
		System.out.flush();
	}
}