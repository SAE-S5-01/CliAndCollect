package fr.iutrodez.sae501.cliandcollect.requetes;

import java.util.List;
import java.util.Map;

public interface VolleyCallback {
    void onSuccess(List<Map<String , String>> results);
    void onError(String error);
}
