package triage.text;

import static org.assertj.core.api.Assertions.assertThat;

import models.cms.ControlEntry;
import models.cms.GlobalDefinition;
import models.cms.LocalDefinition;
import org.junit.Test;

import java.util.Optional;

public class RichTextParserImplTest {

  private static final String STAGE_ID = "1";
  private static final String CONTROL_ENTRY_ID = "2";

  private static final String LASERS_DEFINITION_ID = "1";
  private static final String LASERS_LOCAL_DEFINITION_ID = "11";
  private static final String GUNS_DEFINITION_ID = "2";
  private static final String MISSILES_DEFINITION_ID = "3";
  private static final String RADIO_CONTROLLER_DEFINITION_ID = "4";

  private static final String ML1_ID = "1";
  private static final String PL9001_ID = "2";
  private static final String _1A001_ID = "3";

  private final RichTextParserImpl richTextParser = new RichTextParserImpl(new ParserLookupServiceMock());

  private static ControlEntry createControlEntry(String id, String code) {
    return new ControlEntry().setId(Long.parseLong(id)).setControlCode(code);
  }

  private static Optional<GlobalDefinition> createGlobalDefinition(String id, String term) {
    return Optional.of(new GlobalDefinition().setId(Long.parseLong(id)).setTerm(term));
  }

  private static Optional<LocalDefinition> createLocalDefinition(String id, String term) {
    return  Optional.of(new LocalDefinition().setId(Long.parseLong(id)).setTerm(term));
  }

  private static class ParserLookupServiceMock implements ParserLookupService {
    @Override
    public Optional<ControlEntry> getControlEntryForCode(String code) {
      switch (code.toUpperCase()) {
        case "ML1": return Optional.of(createControlEntry(ML1_ID, code));
        case "PL9001": return Optional.of(createControlEntry(PL9001_ID, code));
        case "1A001": return Optional.of(createControlEntry(_1A001_ID, code));
        default: return Optional.empty();
      }
    }

    @Override
    public Optional<GlobalDefinition> getGlobalDefinitionForTerm(String term) {
      switch (term.toLowerCase()) {
        case "lasers": return createGlobalDefinition(LASERS_DEFINITION_ID, "lasers");
        case "guns'": return createGlobalDefinition(GUNS_DEFINITION_ID, "guns");
        case "missiles": return createGlobalDefinition(MISSILES_DEFINITION_ID, "missiles");
        case "radio controllers": return createGlobalDefinition(RADIO_CONTROLLER_DEFINITION_ID, "radio controllers");
        default: return Optional.empty();
      }
    }

    @Override
    public Optional<LocalDefinition> getLocalDefinitionForTerm(String term, String controlEntryId) {
      if ("lasers".equals(term.toLowerCase())) {
        return createLocalDefinition(LASERS_LOCAL_DEFINITION_ID, "lasers");
      } else {
        return Optional.empty();
      }
    }
  }

  @Test
  public void testParseGlobalDefinition() {
    RichText richText = richTextParser.parseForStage("\"Lasers\" trailing text", STAGE_ID);

    assertThat(richText.getRichTextNodes()).hasSize(2);
    RichTextNode node0 = richText.getRichTextNodes().get(0);
    assertThat(node0).isInstanceOf(DefinitionReferenceNode.class);
    assertThat(node0.getTextContent()).isEqualTo("\"Lasers\"");
    assertThat(((DefinitionReferenceNode) node0).getReferencedDefinitionId()).isEqualTo(LASERS_DEFINITION_ID);
    assertThat(((DefinitionReferenceNode) node0).isGlobal()).isTrue();

    RichTextNode node1 = richText.getRichTextNodes().get(1);
    assertThat(node1).isInstanceOf(SimpleTextNode.class);
    assertThat(node1.getTextContent()).isEqualTo(" trailing text");
  }

  @Test
  public void testParseMultipleWordGlobalDefinition() {
    RichText richText = richTextParser.parseForStage("\"Radio controllers\" trailing text", STAGE_ID);

    assertThat(richText.getRichTextNodes()).hasSize(2);
    RichTextNode node0 = richText.getRichTextNodes().get(0);
    assertThat(node0).isInstanceOf(DefinitionReferenceNode.class);
    assertThat(node0.getTextContent()).isEqualTo("\"Radio controllers\"");
    assertThat(((DefinitionReferenceNode) node0).getReferencedDefinitionId()).isEqualTo(RADIO_CONTROLLER_DEFINITION_ID);
    assertThat(((DefinitionReferenceNode) node0).isGlobal()).isTrue();

    RichTextNode node1 = richText.getRichTextNodes().get(1);
    assertThat(node1).isInstanceOf(SimpleTextNode.class);
    assertThat(node1.getTextContent()).isEqualTo(" trailing text");
  }

  @Test
  public void testParseMultipleGlobalDefinitions() {
    RichText richText = richTextParser.parseForStage("Leading text \"lasers\" and \"missiles\" trailing text", STAGE_ID);

    assertThat(richText.getRichTextNodes()).hasSize(5);

    RichTextNode node0 = richText.getRichTextNodes().get(0);
    assertThat(node0).isInstanceOf(SimpleTextNode.class);
    assertThat(node0.getTextContent()).isEqualTo("Leading text ");

    RichTextNode node1 = richText.getRichTextNodes().get(1);
    assertThat(node1).isInstanceOf(DefinitionReferenceNode.class);
    assertThat(node1.getTextContent()).isEqualTo("\"lasers\"");
    assertThat(((DefinitionReferenceNode) node1).getReferencedDefinitionId()).isEqualTo(LASERS_DEFINITION_ID);
    assertThat(((DefinitionReferenceNode) node1).isGlobal()).isTrue();

    RichTextNode node2 = richText.getRichTextNodes().get(2);
    assertThat(node2).isInstanceOf(SimpleTextNode.class);
    assertThat(node2.getTextContent()).isEqualTo(" and ");

    RichTextNode node3 = richText.getRichTextNodes().get(3);
    assertThat(node3).isInstanceOf(DefinitionReferenceNode.class);
    assertThat(node3.getTextContent()).isEqualTo("\"missiles\"");
    assertThat(((DefinitionReferenceNode) node3).getReferencedDefinitionId()).isEqualTo(MISSILES_DEFINITION_ID);
    assertThat(((DefinitionReferenceNode) node3).isGlobal()).isTrue();

    RichTextNode node4 = richText.getRichTextNodes().get(4);
    assertThat(node4).isInstanceOf(SimpleTextNode.class);
    assertThat(node4.getTextContent()).isEqualTo(" trailing text");
  }

  @Test
  public void testParseControlEntry() {
    RichText richText = richTextParser.parseForStage("Leading text ML1 trailing text", STAGE_ID);

    assertThat(richText.getRichTextNodes()).hasSize(3);

    RichTextNode node0 = richText.getRichTextNodes().get(0);
    assertThat(node0).isInstanceOf(SimpleTextNode.class);
    assertThat(node0.getTextContent()).isEqualTo("Leading text ");

    RichTextNode node1 = richText.getRichTextNodes().get(1);
    assertThat(node1).isInstanceOf(ControlEntryReferenceNode.class);
    assertThat(node1.getTextContent()).isEqualTo("ML1");
    assertThat(((ControlEntryReferenceNode) node1).getControlEntryId()).isEqualTo(ML1_ID);

    RichTextNode node2 = richText.getRichTextNodes().get(2);
    assertThat(node2).isInstanceOf(SimpleTextNode.class);
    assertThat(node2.getTextContent()).isEqualTo(" trailing text");
  }

  @Test
  public void testParseMultipleControlEntries() {
    RichText richText = richTextParser.parseForStage("ML1, PL9001 and 1A001", STAGE_ID);

    assertThat(richText.getRichTextNodes()).hasSize(5);

    RichTextNode node0 = richText.getRichTextNodes().get(0);
    assertThat(node0).isInstanceOf(ControlEntryReferenceNode.class);
    assertThat(node0.getTextContent()).isEqualTo("ML1");
    assertThat(((ControlEntryReferenceNode) node0).getControlEntryId()).isEqualTo(ML1_ID);

    RichTextNode node1 = richText.getRichTextNodes().get(1);
    assertThat(node1).isInstanceOf(SimpleTextNode.class);
    assertThat(node1.getTextContent()).isEqualTo(", ");

    RichTextNode node2 = richText.getRichTextNodes().get(2);
    assertThat(node2).isInstanceOf(ControlEntryReferenceNode.class);
    assertThat(node2.getTextContent()).isEqualTo("PL9001");
    assertThat(((ControlEntryReferenceNode) node2).getControlEntryId()).isEqualTo(PL9001_ID);

    RichTextNode node3 = richText.getRichTextNodes().get(3);
    assertThat(node3).isInstanceOf(SimpleTextNode.class);
    assertThat(node3.getTextContent()).isEqualTo(" and ");

    RichTextNode node4 = richText.getRichTextNodes().get(4);
    assertThat(node4).isInstanceOf(ControlEntryReferenceNode.class);
    assertThat(node4.getTextContent()).isEqualTo("1A001");
    assertThat(((ControlEntryReferenceNode) node4).getControlEntryId()).isEqualTo(_1A001_ID);
  }

  @Test
  public void testParseLocalDefinition() {
    RichText richText = richTextParser.parseForControlEntry("'Lasers' trailing text", CONTROL_ENTRY_ID);

    assertThat(richText.getRichTextNodes()).hasSize(2);
    RichTextNode node0 = richText.getRichTextNodes().get(0);
    assertThat(node0).isInstanceOf(DefinitionReferenceNode.class);
    assertThat(node0.getTextContent()).isEqualTo("'Lasers'");
    assertThat(((DefinitionReferenceNode) node0).getReferencedDefinitionId()).isEqualTo(LASERS_LOCAL_DEFINITION_ID);
    assertThat(((DefinitionReferenceNode) node0).isGlobal()).isFalse();

    RichTextNode node1 = richText.getRichTextNodes().get(1);
    assertThat(node1).isInstanceOf(SimpleTextNode.class);
    assertThat(node1.getTextContent()).isEqualTo(" trailing text");
  }

  @Test
  public void testParseGlobalDefinitionAndLocalDefinitionAndControlEntry() {
    RichText richText = richTextParser.parseForControlEntry("Leading text 'lasers' and \"missiles\" and ML1 trailing text", CONTROL_ENTRY_ID);

    assertThat(richText.getRichTextNodes()).hasSize(7);

    RichTextNode node0 = richText.getRichTextNodes().get(0);
    assertThat(node0).isInstanceOf(SimpleTextNode.class);
    assertThat(node0.getTextContent()).isEqualTo("Leading text ");

    RichTextNode node1 = richText.getRichTextNodes().get(1);
    assertThat(node1).isInstanceOf(DefinitionReferenceNode.class);
    assertThat(node1.getTextContent()).isEqualTo("'lasers'");
    assertThat(((DefinitionReferenceNode) node1).getReferencedDefinitionId()).isEqualTo(LASERS_LOCAL_DEFINITION_ID);
    assertThat(((DefinitionReferenceNode) node1).isGlobal()).isFalse();

    RichTextNode node2 = richText.getRichTextNodes().get(2);
    assertThat(node2).isInstanceOf(SimpleTextNode.class);
    assertThat(node2.getTextContent()).isEqualTo(" and ");

    RichTextNode node3 = richText.getRichTextNodes().get(3);
    assertThat(node3).isInstanceOf(DefinitionReferenceNode.class);
    assertThat(node3.getTextContent()).isEqualTo("\"missiles\"");
    assertThat(((DefinitionReferenceNode) node3).getReferencedDefinitionId()).isEqualTo(MISSILES_DEFINITION_ID);
    assertThat(((DefinitionReferenceNode) node3).isGlobal()).isTrue();

    RichTextNode node4 = richText.getRichTextNodes().get(4);
    assertThat(node4).isInstanceOf(SimpleTextNode.class);
    assertThat(node4.getTextContent()).isEqualTo(" and ");

    RichTextNode node5 = richText.getRichTextNodes().get(5);
    assertThat(node5).isInstanceOf(ControlEntryReferenceNode.class);
    assertThat(node5.getTextContent()).isEqualTo("ML1");
    assertThat(((ControlEntryReferenceNode) node5).getControlEntryId()).isEqualTo(ML1_ID);

    RichTextNode node6 = richText.getRichTextNodes().get(6);
    assertThat(node6).isInstanceOf(SimpleTextNode.class);
    assertThat(node6.getTextContent()).isEqualTo(" trailing text");
  }

  @Test
  public void testParseGlobalDefinition_handlesSingleQuoteWithinDefinition() {
    RichText richText = richTextParser.parseForControlEntry("\"Guns'\" trailing text", CONTROL_ENTRY_ID);

    assertThat(richText.getRichTextNodes()).hasSize(2);

    RichTextNode node0 = richText.getRichTextNodes().get(0);
    assertThat(node0).isInstanceOf(DefinitionReferenceNode.class);
    assertThat(node0.getTextContent()).isEqualTo("\"Guns'\"");
    assertThat(((DefinitionReferenceNode) node0).getReferencedDefinitionId()).isEqualTo(GUNS_DEFINITION_ID);
    assertThat(((DefinitionReferenceNode) node0).isGlobal()).isTrue();

    RichTextNode node1 = richText.getRichTextNodes().get(1);
    assertThat(node1).isInstanceOf(SimpleTextNode.class);
    assertThat(node1.getTextContent()).isEqualTo(" trailing text");
  }

  @Test
  public void testParseControlEntry_unmatchedCode() {
    //ML2 is not a match
    RichText richText = richTextParser.parseForStage("Leading text ML2 trailing text", STAGE_ID);

    assertThat(richText.getRichTextNodes()).hasSize(1);

    RichTextNode node0 = richText.getRichTextNodes().get(0);
    assertThat(node0).isInstanceOf(SimpleTextNode.class);
    assertThat(node0.getTextContent()).isEqualTo("Leading text ML2 trailing text");
  }

  @Test
  public void testParseModalContentLink_parseForControlEntry_single() {
    RichText richText = richTextParser.parseForControlEntry("[example](https://example.org)", CONTROL_ENTRY_ID);

    assertThat(richText.getRichTextNodes()).hasSize(1);

    RichTextNode node0 = richText.getRichTextNodes().get(0);
    assertThat(node0).isInstanceOf(ModalContentLinkNode.class);
    assertThat(node0.getTextContent()).isEqualTo("example");

    ModalContentLinkNode modalNode0 = (ModalContentLinkNode) node0;
    assertThat(modalNode0.getContentId()).isEqualTo("https://example.org");
    assertThat(modalNode0.getLinkText()).isEqualTo("example");
    assertThat(modalNode0.getTextContent()).isEqualTo("example");
  }

  @Test
  public void testParseModalContentLink_parseForControlEntry_multiple() {
    RichText richText = richTextParser.parseForControlEntry("[example0](https://0.example.org)[example1](https://1.example.org)", CONTROL_ENTRY_ID);

    assertThat(richText.getRichTextNodes()).hasSize(2);

    RichTextNode node0 = richText.getRichTextNodes().get(0);
    assertThat(node0).isInstanceOf(ModalContentLinkNode.class);
    assertThat(node0.getTextContent()).isEqualTo("example0");

    ModalContentLinkNode modalNode0 = (ModalContentLinkNode) node0;
    assertThat(modalNode0.getContentId()).isEqualTo("https://0.example.org");
    assertThat(modalNode0.getLinkText()).isEqualTo("example0");
    assertThat(modalNode0.getTextContent()).isEqualTo("example0");

    RichTextNode node1 = richText.getRichTextNodes().get(1);
    assertThat(node1).isInstanceOf(ModalContentLinkNode.class);
    assertThat(node1.getTextContent()).isEqualTo("example1");

    ModalContentLinkNode modalNode1 = (ModalContentLinkNode) node1;
    assertThat(modalNode1.getContentId()).isEqualTo("https://1.example.org");
    assertThat(modalNode1.getLinkText()).isEqualTo("example1");
    assertThat(modalNode1.getTextContent()).isEqualTo("example1");
  }

  @Test
  public void testParseModalContentLink_parseForStage_single() {
    RichText richText = richTextParser.parseForStage("[example](https://example.org)", STAGE_ID);

    assertThat(richText.getRichTextNodes()).hasSize(1);

    RichTextNode node0 = richText.getRichTextNodes().get(0);
    assertThat(node0).isInstanceOf(ModalContentLinkNode.class);
    assertThat(node0.getTextContent()).isEqualTo("example");

    ModalContentLinkNode modalNode0 = (ModalContentLinkNode) node0;
    assertThat(modalNode0.getContentId()).isEqualTo("https://example.org");
    assertThat(modalNode0.getLinkText()).isEqualTo("example");
    assertThat(modalNode0.getTextContent()).isEqualTo("example");
  }
}