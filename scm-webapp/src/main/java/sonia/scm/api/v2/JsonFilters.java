package sonia.scm.api.v2;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Maps;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import static java.util.Arrays.stream;

final class JsonFilters {

  private JsonFilters() {
  }

  static void filterByFields(JsonNode root, Collection<String> filterExpressions) {
    createJsonFilterNode(filterExpressions).filterNode(root);
  }

  private static JsonFilterNode createJsonFilterNode(Collection<String> filterExpressions) {
    JsonFilterNode rootFilterNode = new JsonFilterNode();
    filterExpressions.stream()
      .map(JsonFilterNode::expressionPartIterator)
      .forEach(rootFilterNode::appendFilterExpression);
    return rootFilterNode;
  }

  private static class JsonFilterNode {

    private final Map<String,JsonFilterNode> children = Maps.newHashMap();

    private static Iterator<String> expressionPartIterator(String filterExpression) {
      return stream(filterExpression.split("\\.")).iterator();
    }

    private void appendFilterExpression(Iterator<String> fields) {
      if (fields.hasNext()) {
        addOrGet(fields.next()).appendFilterExpression(fields);
      }
    }

    private void filterNode(JsonNode node) {
      if (!isLeaf()) {
        if (node.isObject()) {
          filterObjectNode((ObjectNode) node);
        } else if (node.isArray()) {
          filterArrayNode((ArrayNode) node);
        }
      }
    }

    private void filterObjectNode(ObjectNode objectNode) {
      Iterator<Map.Entry<String,JsonNode>> entryIterator = objectNode.fields();
      while (entryIterator.hasNext()) {
        Map.Entry<String,JsonNode> entry = entryIterator.next();

        JsonFilterNode childFilter = get(entry.getKey());
        if (childFilter == null) {
          entryIterator.remove();
        } else {
          childFilter.filterNode(entry.getValue());
        }
      }
    }

    private void filterArrayNode(ArrayNode arrayNode) {
      arrayNode.forEach(this::filterNode);
    }

    private JsonFilterNode addOrGet(String name) {
      JsonFilterNode child = children.get(name);
      if (child == null) {
        child = new JsonFilterNode();
        children.put(name, child);
      }
      return child;
    }

    JsonFilterNode get(String name) {
      return children.get(name);
    }

    boolean isLeaf() {
      return children.isEmpty();
    }
  }

}
