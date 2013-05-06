package models;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 06.05.13
 * Time: 3:26
 */
public class ContestResult { //TODO generalize

    private int r;
    private int w;
    private int n;
    private int scores;
    private int bonus;
    private int discount;

    public ContestResult(int r, int w, int n, int scores, int bonus, int discount) {
        this.r = r;
        this.w = w;
        this.n = n;
        this.scores = scores;
        this.bonus = bonus;
        this.discount = discount;
    }

    public int getR() {
        return r;
    }

    public int getW() {
        return w;
    }

    public int getN() {
        return n;
    }

    public int getScores() {
        return scores;
    }

    public int getBonus() {
        return bonus;
    }

    public int getDiscount() {
        return discount;
    }
}
