package app.application;

import java.util.Timer;
import java.util.TimerTask;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.messages.RegistryAuth;

import app.exception.PapWebRunTimeException;

public class PaprikaTimer extends TimerTask
{
    private int n = 0;
	
    @Override
    public void run()
    {
    
       
    }
    
    public boolean removeContainer(String id) {
		boolean removed = true;
		PaprikaFacade facade= PaprikaFacade.getInstance();
		try {
			RegistryAuth registryAuth = RegistryAuth.builder().serverAddress(facade.getHostName()).build();
			DockerClient docker;

			docker = DefaultDockerClient.fromEnv().dockerAuth(false).registryAuth(registryAuth).build();
			if (!docker.inspectContainer(id).state().running()) {

				docker.removeContainer(id);
				PaprikaWebMain.addVersionOnAnalyze(-1);

				facade.launchContainer(docker);

			} else
				removed = false;

			docker.close();

		} catch (Exception e) {
			removed = false;
			PaprikaWebMain.LOGGER.error(e.getMessage(), e);
			throw new PapWebRunTimeException(e.getMessage());
		}
		PaprikaWebMain.LOGGER.trace("Work?");
		return removed;
	}
	
    public static void main(String[] args)
    {
    	PaprikaTimer task = new PaprikaTimer();
        Timer timer = new Timer();
        timer.schedule(task, 0, 120000);//2 Minutes
     }
}