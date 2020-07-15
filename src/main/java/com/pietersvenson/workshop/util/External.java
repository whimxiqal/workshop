/*
 * MIT License
 *
 * Copyright (c) 2020 Pieter Svenson
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package com.pietersvenson.workshop.util;

import com.pietersvenson.workshop.Workshop;
import org.bukkit.Bukkit;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.URL;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public final class External {

  private External() {
  }

  public static CompletableFuture<Optional<UUID>> getPlayerUuid(String username) {
    CompletableFuture<Optional<UUID>> out = CompletableFuture.supplyAsync(() -> {
      try {
        URL obj = new URL("https://api.mojang.com/users/profiles/minecraft/" + username);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        int responseCode = con.getResponseCode();
        String response;
        if (responseCode == HttpURLConnection.HTTP_OK) { // success
          BufferedReader in = new BufferedReader(new InputStreamReader(
              con.getInputStream()));
          String inputLine;
          StringBuilder responseBuilder = new StringBuilder();

          while ((inputLine = in.readLine()) != null) {
            responseBuilder.append(inputLine);
          }
          in.close();

          response = responseBuilder.toString();
        } else {
          return Optional.empty();
        }
        if (response.isEmpty()) {
          return Optional.empty();
        }
        String basicUuid = ((JSONObject) JSONValue.parseWithException(response)).get("id").toString();
        return Optional.of(UUID.fromString(basicUuid.substring(0, 8)
            + "-"
            + basicUuid.subSequence(8, 12)
            + "-"
            + basicUuid.subSequence(12, 16)
            + "-"
            + basicUuid.subSequence(16, 20)
            + "-"
            + basicUuid.substring(20)));
      } catch (IOException | ParseException e) {
        e.printStackTrace();
      }

      return Optional.empty();
    });

    Bukkit.getScheduler().runTask(Workshop.getInstance(), () -> {
      try {
        out.get(2, TimeUnit.SECONDS);
      } catch (InterruptedException | ExecutionException | TimeoutException e) {
        Workshop.getInstance().getLogger().throwing("External.java", "getPlayerUuid(UUID)", e);
      }
    });

    return out;

  }

  public static CompletableFuture<Optional<String>> getPlayerName(UUID uuid) {
    CompletableFuture<Optional<String>> out = CompletableFuture.supplyAsync(() -> {
      try {
        URL obj = new URL("https://api.mojang.com/user/profiles/" + uuid.toString().replace("-", "") + "/names");
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        int responseCode = con.getResponseCode();
        String response;
        if (responseCode == HttpURLConnection.HTTP_OK) { // success
          BufferedReader in = new BufferedReader(new InputStreamReader(
              con.getInputStream()));
          String inputLine;
          StringBuilder responseBuilder = new StringBuilder();

          while ((inputLine = in.readLine()) != null) {
            responseBuilder.append(inputLine);
          }
          in.close();

          response = responseBuilder.toString();
        } else {
          return Optional.empty();
        }
        if (response.isEmpty()) {
          return Optional.empty();
        }
        JSONArray historyArray = (JSONArray) JSONValue.parseWithException(response);
        return Optional.of(((JSONObject) historyArray.get(historyArray.size() - 1)).get("name").toString());
      } catch (IOException | ParseException e) {
        Workshop.getInstance().getLogger().throwing("External.java", "getPlayerName(UUID)", e);
      }

      return Optional.empty();
    });

    Bukkit.getScheduler().runTask(Workshop.getInstance(), () -> {
      try {
        out.get(2, TimeUnit.SECONDS);
      } catch (InterruptedException | ExecutionException | TimeoutException e) {
        Workshop.getInstance().getLogger().throwing("External.java", "getPlayerName(UUID)", e);
      }
    });

    return out;

  }

}
