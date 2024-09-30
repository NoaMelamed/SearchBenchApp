public abstract class CountDownTimer extends Object {
    private long millisUntilFinished;
    private long countDownInterval;

    public CountDownTimer(long millisUntilFinished, long countDownInterval) {
        this.countDownInterval = countDownInterval;
        this.millisUntilFinished = millisUntilFinished;
    }

    public abstract void onTick(long millisUntilFinished);

    public abstract void onFinish();

}
