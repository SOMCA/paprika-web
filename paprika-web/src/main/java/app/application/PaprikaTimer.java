package app.application;

import java.net.UnknownHostException;
import java.util.TimerTask;

import org.neo4j.driver.v1.Transaction;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.ContainerInfo;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.RegistryAuth;

/**
 * PaprikaTimer is a Timer who launch the method run each X time. The time is give per the PaprikaWebMain.
 * He launch container of the queue and remove stopped container.
 * 
 * @author guillaume
 *
 */
public class PaprikaTimer extends TimerTask {
	private String[] containerRun;
	private int parallelanalyzeMax;
	private int num = 0;

	/**
	 * 
	 * @param containerRun The container currently run(if server restart)
	 */
	public PaprikaTimer(String[] containerRun) {
		this.containerRun = containerRun;
		if (containerRun != null)
			this.parallelanalyzeMax = containerRun.length;
		else
			this.parallelanalyzeMax = 0;
	}

	/**
	 * Remove the finished container on Docker ( shell: docker ps -a, for see
	 * the id, then docker rm id)
	 * 
	 * The method have three try, for a reason simple, inspectecContainer(id)
	 * return a error, not null, if do not found.
	 * 
	 */
	@Override
	public void run() {
		System.out.println("Timer: Run " + num);
		System.out.flush();
		num++;

		DockerClient docker = null;
		try {
			PaprikaFacade facade = PaprikaFacade.getInstance();

			RegistryAuth registryAuth = RegistryAuth.builder().serverAddress(facade.getHostName()).build();
			docker = DefaultDockerClient.fromEnv().dockerAuth(false).registryAuth(registryAuth).build();
		} catch (DockerCertificateException e) {
			System.out.println(" error DockerCertificateException");
			System.out.flush();
		} catch (UnknownHostException e1) {
			System.out.println(" error UnknownHostException");
			System.out.flush();
		}

		System.out.println("no error docker");
		System.out.flush();
		boolean needUpdate;
		String container;
		try (Transaction tx = PaprikaWebMain.getSession().beginTransaction()) {
			for (int i = 0; i < containerRun.length; i++) {
				needUpdate = false;
				container = containerRun[i];
				if (container == null) {
					System.out.println("launch one");
					System.out.flush();

					containerRun[i] = launchContainer(docker);
					if (containerRun[i] != null) {
						System.out.println("new Container");
						needUpdate = true;
					}

				} else {

					ContainerInfo info = null;

					try {
						info = docker.inspectContainer(container);
					} catch (Exception e) {
					}
					// #TODO bug, do not work, why? No idea. 
					if (info == null) {
						containerRun[i] = null;
						System.out.println("Put null");
						needUpdate = true;

					} else if (!info.state().running()) {
						System.out.println("launch two");
						System.out.flush();

						try {
							docker.removeContainer(container);
						} catch (Exception e) {
						}

						PaprikaWebMain.addVersionOnAnalyze(-1);
						containerRun[i] = launchContainer(docker);
						if (containerRun[i] != null) {
							System.out.println("new Container with remove");
							needUpdate = true;
						}
					}
					if (needUpdate) {
						System.out.println("apply Datasave " + i);
						tx.run("MATCH(n:DataSave) SET n.containerRun" + i + "=\"" + this.containerRun[i]);
					}
				}
			}
			tx.success();
		}
		if (docker != null) {
			docker.close();

		}

	}

	private String launchContainer(DockerClient docker) {
		String newid = null;
		boolean githubok=false;
		PaprikaFacade facade = PaprikaFacade.getInstance();
		if (PaprikaWebMain.getVersionOnAnalyze() < this.parallelanalyzeMax) {
			String[] otherStrContainerConfig = PaprikaWebMain.getContainerqueue().poll();
			if (otherStrContainerConfig != null) {
				
				HostConfig hostConfig = null;
				ContainerConfig otherContainerConfig = null;
				// Github
				if (otherStrContainerConfig.length == 6) {
					githubok=true;
					hostConfig = HostConfig.builder().networkMode("paprikaweb_default")
							.links("neo4j-paprika", "web-paprika").build();

					otherContainerConfig = ContainerConfig.builder().hostConfig(hostConfig)
							.image("paprika-tandoori:latest").cmd(otherStrContainerConfig).workingDir("/dock").build();
				}
				// File.apk
				else {
					hostConfig = HostConfig.builder().networkMode("paprikaweb_default")
							.links("neo4j-paprika", "web-paprika").binds("/tmp/application:/dock/application:ro")
							.build();

					otherContainerConfig = ContainerConfig.builder().hostConfig(hostConfig)
							.image("paprika-analyze:latest").cmd(otherStrContainerConfig).workingDir("/dock").build();
				}
				ContainerCreation creation;
				try {
					creation = docker.createContainer(otherContainerConfig);

					newid = creation.id();
					facade.setParameterOnNode(otherStrContainerConfig[otherStrContainerConfig.length - (githubok?2:1)],
							"idContainer", newid);

					docker.startContainer(newid);
				} catch (DockerException | InterruptedException e) {
					System.out.println("Error");
				}
				PaprikaWebMain.addVersionOnAnalyze(1);
				PaprikaWebMain.LOGGER.trace("container create and start success");

			}
		}
		return newid;
	}

}