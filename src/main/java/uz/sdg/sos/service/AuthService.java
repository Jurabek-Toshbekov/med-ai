package uz.sdg.sos.service;

import java.util.HashMap;

public interface AuthService {


    HashMap<String, Object> login(String login, String password);


}
