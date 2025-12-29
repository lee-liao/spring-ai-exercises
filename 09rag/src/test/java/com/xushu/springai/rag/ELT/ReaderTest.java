package com.xushu.springai.rag.ELT;


import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.ParagraphPdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.List;

@SpringBootTest
public class ReaderTest {

    @Test
    public void testReaderText(@Value("classpath:rag/terms-of-service.txt") Resource resource) {


        TextReader textReader = new TextReader(resource);
        List<Document> documents = textReader.read();

        for (Document document : documents) {
            System.out.println(document.getText());
        }
    }


    @Test
    public void testReaderMD(@Value("classpath:rag/9_横店影视股份有限公司_0.md") Resource resource) {
        MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                .withHorizontalRuleCreateDocument(false)     // 分割线创建新document  false:不会  true：会
                .withIncludeCodeBlock(false)                // 代码创建新document false:会
                .withIncludeBlockquote(false)               // 引用创建新document false:会
                .withAdditionalMetadata("filename", resource.getFilename())    // 每个document添加的元数据
                .build();

        MarkdownDocumentReader markdownDocumentReader = new MarkdownDocumentReader(resource, config);
        List<Document> documents = markdownDocumentReader.read();
        for (Document document : documents) {
            System.out.println(document.getText());
        }
    }


    @Test
    public void testReaderPdf(@Value("classpath:rag/平安银行2023年半年度报告摘要.pdf") Resource resource) {

        PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(resource,
                PdfDocumentReaderConfig.builder().build());

        List<Document> documents = pdfReader.read();
        for (Document document : documents) {
            System.out.println(document.getText());
        }
    }


    // 必需要带目录，  按pdf的目录分document
    @Test
    public void testReaderParagraphPdf(@Value("classpath:rag/平安银行2023年半年度报告.pdf") Resource resource) {
        ParagraphPdfDocumentReader pdfReader = new ParagraphPdfDocumentReader(resource,
                PdfDocumentReaderConfig.builder()
                        // 不同的PDF生成工具可能使用不同的坐标系 ， 如果内容识别有问题， 可以设置该属性为true
                        .withReversedParagraphPosition(true)
                        .withPageTopMargin(0)       // 上边距
                        .withPageExtractedTextFormatter(ExtractedTextFormatter.builder()
                                // 从页面文本中删除前 N 行
                                .withNumberOfTopTextLinesToDelete(0)
                                .build())
                        .build());

        List<Document> documents = pdfReader.read();
        for (Document document : documents) {
            System.out.println(document.getText());
        }
    }


}
