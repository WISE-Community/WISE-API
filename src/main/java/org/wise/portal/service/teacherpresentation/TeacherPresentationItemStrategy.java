package org.wise.portal.service.teacherpresentation;

import java.util.List;

import org.wise.portal.domain.group.Group;
import org.wise.portal.domain.run.Run;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Strategy interface defining serialization, deserialization, validation, and ID extraction
 * for different teacher presentation item types (e.g. Discussion, Ideas).
 *
 * @param <T> the typed item domain class
 */
public interface TeacherPresentationItemStrategy<T> {

  /**
   * Returns the unique presentation config type string handled by this strategy
   * (e.g. "Discussion", "Ideas").
   */
  String getType();

  /**
   * Deserializes the JSON items string into a typed list of items.
   *
   * @param json the serialized JSON items string
   * @param objectMapper the Jackson object mapper
   * @return list of parsed items, or empty list on null/empty/invalid input
   */
  List<T> parseItems(String json, ObjectMapper objectMapper);

  /**
   * Serializes a list of items into a JSON string.
   *
   * @param items the list of typed items to serialize
   * @param objectMapper the Jackson object mapper
   * @return serialized JSON string
   */
  String serializeItems(List<T> items, ObjectMapper objectMapper);

  /**
   * Validates and normalizes raw input items (e.g. from service or controller).
   *
   * @param run the active run
   * @param period the active period group
   * @param rawItems the raw input items list
   * @return list of normalized typed items
   * @throws IllegalArgumentException if items fail validation rules
   */
  List<T> validateAndNormalize(Run run, Group period, List<?> rawItems);

  /**
   * Extracts student work IDs referenced by the given items.
   *
   * @param items the list of typed items
   * @return list of student work IDs
   */
  List<Integer> extractStudentWorkIds(List<T> items);
}
