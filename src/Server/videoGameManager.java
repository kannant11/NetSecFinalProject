package Server;

import merrimackutil.json.JsonIO;
import merrimackutil.json.types.JSONObject;
import merrimackutil.json.types.JSONArray;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class videoGameManager {
    private final List<JSONObject> games;
    private final String filePath;

    public videoGameManager(String configFilePath) throws Exception{
        JSONObject config = JsonIO.readObject(new File(configFilePath));
        this.filePath = config.getString("games files");
        this.games = loadGames();
    }

    private List<JSONObject> loadGames() throws Exception{
        List<JSONObject> gameList = new ArrayList<>();
        JSONObject gamesData = JsonIO.readObject(new File(filePath));
        JSONArray entries = gamesData.getArray("entries");

        for(int i = 0; i < entries.size(); i++){
            JSONObject game = entries.getObject(i);
            gameList.add(game);
        }

        return gameList;

    }

    public List<JSONObject> getAllGames(){
        return games;
    }
       
    

    
}
