package Server;

import merrimackutil.json.JsonIO;
import merrimackutil.json.types.JSONObject;
import java.io.File;

public class loadConfig {
    private final JSONObject config;

    public loadConfig(String configPath)throws Exception{
        this.config = JsonIO.readObject(new File(configPath));
    }

    public String getFilePath(){
        return config.getString("user_file");
    }

    public String gamesFilePath(){
        return config.getString("game_files");
    }

    public int getServerPort(){
        return config.getInt("port");
    }
}
