package com.example.SynCalendar;


import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.ai.client.generativeai.Chat;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.ai.client.generativeai.type.ImagePart;
import com.google.ai.client.generativeai.type.Part;
import com.google.ai.client.generativeai.type.RequestOptions;
import com.google.ai.client.generativeai.type.TextPart;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;

import kotlin.Result;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.coroutines.EmptyCoroutineContext;

import org.json.JSONException;
import org.json.JSONObject;

public class GeminiManager {
    private static final String API_KEY = "AIzaSyDQDBWloTVJzNyiawqiQtrINOqhvusxi4s";
    private static String SYSTEM_PROMPT  = "You are a helpful assistant.";
    private CoroutineContext coroutineContext;
    private Handler handler = new ConsoleHandler();
    public interface GeminiCallback{
        void onSuccessful(String response);
        void onError(Throwable ex);
    }
    private static GeminiManager instance;
    private GenerativeModel generativeModel;
    private Chat chat;
    public static GeminiManager getInstance(String prompt){
        if (instance == null) instance = new GeminiManager(prompt);
        return instance;
    }

    public static GeminiManager getInstance(){
        return instance;
    }

    private GeminiManager(String system_prompt){
        List<Part> parts = new ArrayList<Part>();
        parts.add(new TextPart("You are an appointment creator. Your task is to parse natural language into structured event data. " + 
                              "Always return a valid JSON object with the event details. " + 
                              "If you can't understand the input, return a JSON object with an 'error' field explaining why. " +
                              "Format dates in ISO 8601 format with UTC timezone. " + system_prompt));
        generativeModel =  new GenerativeModel(
                "gemini-2.0-flash",
                API_KEY,
                /* generation config */ null,
                /* safety setting */ null,
                /* request options */ new RequestOptions(),
                /* tools */ null,
                /* tool config */ null,
                /* system prompt */ new Content(parts));
        coroutineContext = EmptyCoroutineContext.INSTANCE;
        startChat();
        Log.d("GeminiManager", "Initialized with system prompt");
    }

    public void sendMessage(String prompt, GeminiCallback geminiCallback){
        Log.d("GeminiManager", "Sending message to Gemini: " + prompt);
        String enhancedPrompt = "Parse this event description and return a VALID JSON object with these fields if mentioned: " +
                               "title (string), details (string), address (string), topic (string), " +
                               "start (ISO 8601 datetime string), duration (integer minutes), " +
                               "reminder (boolean), remTime (ISO 8601 datetime string), " +
                               "and sharedWith (array of strings containing usernames to share with). " +
                               "Example format: {" +
                               "\"title\":\"Meeting\"," +
                               "\"start\":\"2024-03-20T14:00:00.000Z\"," +
                               "\"duration\":60," +
                               "\"sharedWith\":[\"john\",\"mary\"]" +
                               "}. " +
                               "Extract any mentioned people to share with into the sharedWith array. " +
                               "Here's the event description: " + prompt;
        
        generativeModel.generateContent(enhancedPrompt, new Continuation<GenerateContentResponse>() {
            @NonNull
            @Override
            public CoroutineContext getContext() {
                return EmptyCoroutineContext.INSTANCE;
            }

            @Override
            public void resumeWith(@NonNull Object result) {
                if (result instanceof Result.Failure) {
                    Result.Failure failure = (Result.Failure) result;
                    Log.e("GeminiManager", "Error generating content", failure.exception);
                    geminiCallback.onError(failure.exception);
                }
                else {
                    GenerateContentResponse generateContentResponse = (GenerateContentResponse)result;
                    String response = generateContentResponse.getText();
                    Log.d("GeminiManager", "Raw response from Gemini: " + response);
                    
                    // Try to clean up the response if it contains markdown code blocks
                    if (response.contains("```json")) {
                        response = response.split("```json")[1].split("```")[0].trim();
                    } else if (response.contains("```")) {
                        response = response.split("```")[1].split("```")[0].trim();
                    }
                    
                    // Validate JSON format
                    try {
                        // Try parsing and re-stringifying to ensure valid JSON
                        JSONObject jsonObject = new JSONObject(response);
                        response = jsonObject.toString();
                        Log.d("GeminiManager", "Cleaned and validated JSON response: " + response);
                    } catch (JSONException e) {
                        Log.e("GeminiManager", "Invalid JSON response: " + response, e);
                        // Create an error JSON response
                        try {
                            JSONObject errorJson = new JSONObject();
                            errorJson.put("error", "Failed to parse response");
                            errorJson.put("title", "Error Processing Speech");
                            response = errorJson.toString();
                        } catch (JSONException ex) {
                            Log.e("GeminiManager", "Error creating error JSON", ex);
                        }
                    }
                    
                    geminiCallback.onSuccessful(response);
                }
            }
        });
    }

    public void sendMessageWithPhoto(String propmt, Bitmap bitmap, GeminiCallback geminiCallback){
        List<Part> parts = new ArrayList<>();
        parts.add(new TextPart(propmt));
        parts.add(new ImagePart(bitmap));
        Content[] content = new Content[1];
        content[0] = new Content(parts);
        generativeModel.generateContent(content,
                new Continuation<GenerateContentResponse>() {
                    @NonNull
                    @Override
                    public CoroutineContext getContext() {
                        return EmptyCoroutineContext.INSTANCE;
                    }

                    @Override
                    public void resumeWith(@NonNull Object result) {
                        if (result instanceof Result.Failure) {
                            Result.Failure failure = (Result.Failure) result;
                            geminiCallback.onError(failure.exception);
                        }
                        else{
                            GenerateContentResponse generateContentResponse = (GenerateContentResponse)result;
                            geminiCallback.onSuccessful(generateContentResponse.getText());
                        }
                    }
                });
    }

    private void startChat(){
        chat = generativeModel.startChat(Collections.emptyList());
    }

    public void sendChatMessage(String prompt, GeminiCallback geminiCallback){
        chat.sendMessage(prompt, new Continuation<GenerateContentResponse>() {
            @NonNull
            @Override
            public CoroutineContext getContext() {
                return coroutineContext;
            }

            @Override
            public void resumeWith(@NonNull Object result) {
                if (result instanceof Result.Failure) {
                    Result.Failure failure = (Result.Failure) result;
                    geminiCallback.onError(failure.exception);
                }
                else{
                    GenerateContentResponse generateContentResponse = (GenerateContentResponse)result;
                    geminiCallback.onSuccessful(generateContentResponse.getText());
                }
            }
        });
    }

    public void setSystemPrompt(String prompt){
        SYSTEM_PROMPT = prompt;
    }
}


