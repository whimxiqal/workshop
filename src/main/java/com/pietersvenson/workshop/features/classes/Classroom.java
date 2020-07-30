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

package com.pietersvenson.workshop.features.classes;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.pietersvenson.workshop.Workshop;
import com.pietersvenson.workshop.config.Settings;
import com.pietersvenson.workshop.util.External;
import com.pietersvenson.workshop.util.Format;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.annotation.Nonnull;
import java.text.ParseException;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Classroom {

  @Getter
  @NonNull
  private final String id;
  @Getter
  @Setter
  @NonNull
  private String name;
  @Getter
  @Setter
  @NonNull
  private Curriculum curriculum = Curriculum.NONE;
  @Getter
  @Setter
  @NonNull
  private Schedule schedule = Schedule.empty();
  @Getter
  @NonNull
  private final List<Participant> participants = Lists.newLinkedList();
  private Map<UUID, RegistrationForm> registering = Maps.newHashMap();
  private boolean publik;

  public Classroom(@Nonnull String id) {
    this.id = Objects.requireNonNull(id);
    this.name = Objects.requireNonNull(id);
  }

  public boolean addParticipant(@Nonnull Participant participant) {
    return participants.add(Objects.requireNonNull(participant));
  }

  public boolean addParticipants(@Nonnull Collection<Participant> participant) {
    return participants.addAll(Objects.requireNonNull(participant));
  }

  /**
   * Removes all participants with this uuid.
   *
   * @param playerUuid the uuid of the player
   * @return true if any participants were removed
   */
  public boolean removeParticipant(UUID playerUuid) {
    return participants.removeIf(participant -> participant.getPlayerUuid().equals(playerUuid));
  }

  public boolean inClass(@Nonnull UUID playerUuid) {
    return participants.stream().anyMatch(participant ->
        Objects.requireNonNull(playerUuid).equals(participant.getPlayerUuid()));
  }

  public boolean inSession() {
    return schedule.includes(Instant.now());
  }

  public boolean conflicts(@Nonnull Classroom classroom) {
    return this.schedule.overlaps(classroom.schedule);
  }

  public boolean isRegistered(UUID uuid) {
    return participants.stream().map(Participant::getPlayerUuid).anyMatch(u -> u.equals(uuid));
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Classroom)) {
      return false;
    }
    return id.equals(((Classroom) obj).id);
  }

  @Override
  public String toString() {
    return id;
  }

  public Map.Entry<String, Map<String, Object>> serialize() {
    Map.Entry<String, Map<String, Object>> out = Maps.immutableEntry(id, Maps.newTreeMap());
    out.getValue().put("name", name);
    out.getValue().put("curriculum", curriculum.name());
    out.getValue().put("schedule", schedule.serialize());
    out.getValue().put("participants", participants.stream().map(Participant::serialize).collect(Collectors.toList()));
    return out;
  }

  public static Classroom deserialize(String id, Map<String, Object> data) throws ParseException, Schedule.OverlappingAppointmentException {
    Classroom classroom = new Classroom(id);
    classroom.setName(Optional.ofNullable(data.get("name"))
        .filter(name -> name instanceof String)
        .map(name -> ((String) name))
        .orElse(id));
    classroom.setCurriculum(Optional.ofNullable(data.get("curriculum"))
        .filter(curr -> curr instanceof String)
        .map(curr -> Curriculum.valueOf((String) curr))
        .orElse(Curriculum.NONE));
    classroom.setSchedule(Schedule.deserialize((List<Object>) data.get("schedule")));
    classroom.addParticipants(((List<Object>) data.get("participants"))
        .stream()
        .map(o -> Participant.deserialize((Map<String, String>) o))
        .collect(Collectors.toList()));
    return classroom;
  }

  public static boolean isValidName(String s) {
    return !s.isEmpty() && !Pattern.compile(".*[^a-zA-Z0-9 ].*").matcher(s).find();
  }

  public Optional<RegistrationForm> getRegistrationForm(UUID player) {
    return Optional.ofNullable(this.registering.get(player));
  }

  public boolean isRegistering(UUID player) {
    return getRegistrationForm(player).isPresent();
  }

  public void startRegistering(UUID player) {
    this.registering.put(player, new RegistrationForm());
  }

  public Optional<Participant> completeRegistration(UUID player) {
    Optional<RegistrationForm> form = getRegistrationForm(player);
    if (!form.isPresent()) {
      return Optional.empty();
    }
    Participant participant = form.get().complete(player);
    this.addParticipant(participant);
    registering.remove(player);
    return Optional.of(participant);
  }

  public void setPublic(boolean publik) {
    this.publik = publik;
  }

  public boolean isPublic() {
    return this.publik;
  }

  @Data
  public static class Participant {

    private final String firstName;
    private final String lastName;
    private final UUID playerUuid;
    @Getter
    @Setter
    private String lastKnownUsername;

    public Participant(@Nonnull String firstName, @Nonnull String lastName, @Nonnull UUID uuid) {
      this.firstName = Objects.requireNonNull(firstName);
      this.lastName = Objects.requireNonNull(lastName);
      this.playerUuid = Objects.requireNonNull(uuid);
      External.getPlayerName(this.getPlayerUuid()).thenAccept(nameOp -> nameOp.ifPresent(this::setLastKnownUsername));
    }

    Map<String, String> serialize() {
      Map<String, String> out = Maps.newTreeMap();
      out.put("first_name", firstName);
      out.put("last_name", lastName);
      out.put("mc_uuid", playerUuid.toString());
      return out;
    }

    static Participant deserialize(Map<String, String> data) {
      Participant out = new Participant(
          data.get("first_name"),
          data.get("last_name"),
          UUID.fromString(data.get("mc_uuid")));
      External.getPlayerName(out.getPlayerUuid()).thenAccept(nameOp -> nameOp.ifPresent(out::setLastKnownUsername));
      return out;
    }

    public boolean giveNickname() {
      if (Settings.ENABLE_NICKNAME_PARTICIPANTS.getValue()) {
        return false;
      }
      try {
        Workshop.getInstance().getState().getNicknameManager().setNickname(playerUuid, Format.participantDisplayName(this));
        return true;
      } catch (UnsupportedOperationException e) {
        return false;
      }
    }
  }
}
