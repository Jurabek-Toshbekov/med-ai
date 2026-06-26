package uz.sdg.sos.dto.makeObjects;


import uz.sdg.sos.base.BaseDto;


public class QuizFlashCard extends BaseDto {
    private String uzb;
    private String japanKanji;
    private String japanHiragana;


    public QuizFlashCard(String uzb, String japanKanji, String japanHiragana) {
        this.uzb = uzb;
        this.japanKanji = japanKanji;
        this.japanHiragana = japanHiragana;
    }

    public String getUzb() {
        return uzb;
    }

    public void setUzb(String uzb) {
        this.uzb = uzb;
    }

    public String getJapanKanji() {
        return japanKanji;
    }

    public void setJapanKanji(String japanKanji) {
        this.japanKanji = japanKanji;
    }

    public String getJapanHiragana() {
        return japanHiragana;
    }

    public void setJapanHiragana(String japanHiragana) {
        this.japanHiragana = japanHiragana;
    }
}
