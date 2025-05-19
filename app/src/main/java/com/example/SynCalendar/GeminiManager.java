package com.example.SynCalendar;


import android.graphics.Bitmap;

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
        parts.add(new TextPart(system_prompt));
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
    }

    public void sendMessage(String prompt, GeminiCallback geminiCallback){
        generativeModel.generateContent(prompt, new Continuation<GenerateContentResponse>() {
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

