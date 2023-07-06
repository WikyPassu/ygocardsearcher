package com.example.ygocardsearcher;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class HiloConexion extends Thread {

    public static final int IMAGEN = 1;
    public static final int CARTAS = 2;
    Handler handler;
    Boolean img;
    String url;
    Activity a;

    public HiloConexion(Activity a, Handler handler, boolean img, String url){
        this.handler = handler;
        this.img = img;
        this.url = url;
        this.a = a;
    }

    @Override
    public void run(){
        try{
            ConexionHTTP conexionHTTP = new ConexionHTTP(this.a);
            if(!img){
                byte[] cartasJson = conexionHTTP.obtenerRespuesta(this.url);
                String s = new String(cartasJson);
                Message msg = new Message();
                msg.arg1 = CARTAS;
                msg.obj = this.parserJson(s);
                this.handler.sendMessage(msg);
            }
            else{
                byte[] img = conexionHTTP.obtenerRespuesta(this.url);
                Message msg = new Message();
                msg.arg1 = IMAGEN;
                msg.obj = img;
                this.handler.sendMessage(msg);
            }
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public List<CartaModel> parserJson(String s){
        List<CartaModel> cartas = new ArrayList<CartaModel>();

        try {
            if(!"".equals(s)){
                JSONObject jsonRespuesta = new JSONObject(s);
                JSONArray jsonArray = jsonRespuesta.getJSONArray("data");

                for(int i=0; i<jsonArray.length(); i++){
                    CartaModel carta = new CartaModel();
                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    if("Skill Card".equals(jsonObject.getString("type"))){
                        continue;
                    }

                    carta.setId(jsonObject.getInt("id"));
                    carta.setName(jsonObject.getString("name"));
                    carta.setType(jsonObject.getString("type"));
                    carta.setDesc(jsonObject.getString("desc"));
                    carta.setRace(jsonObject.getString("race"));

                    if(!"Spell Card".equals(carta.getType()) && !"Trap Card".equals(carta.getType())){
                        carta.setAtk(String.valueOf(jsonObject.getInt("atk")));
                        if("Link Monster".equals(carta.getType())){
                            carta.setDef(String.valueOf(jsonObject.getInt("linkval")));
                            carta.setLevel(null);
                        }
                        else{
                            carta.setDef(String.valueOf(jsonObject.getInt("def")));
                            carta.setLevel(String.valueOf(jsonObject.getInt("level")));
                        }
                        carta.setAttribute(jsonObject.getString("attribute"));
                    }
                    else{
                        carta.setAtk(null);
                        carta.setDef(null);
                        carta.setLevel(null);
                        carta.setAttribute(null);
                    }

                    if(jsonObject.has("archetype")){
                        carta.setArchetype(jsonObject.getString("archetype"));
                    }
                    else{
                        carta.setArchetype(null);
                    }

                    JSONArray jsonArrayImg = jsonObject.getJSONArray("card_images");
                    JSONObject jsonImgUrl = jsonArrayImg.getJSONObject(0);
                    carta.setImage_url(jsonImgUrl.getString("image_url"));

                    cartas.add(carta);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("Error", "Probablemente query mal formada o query sin resultados.");
        }

        return cartas;
    }
}
