import java.io.IOException;
  public void diffForOneRevisionShouldCreateDiff() throws IOException {
    gitDiffCommand.getDiffResult(diffCommandRequest).accept(output);
  public void diffForOneBranchShouldCreateDiff() throws IOException {
    gitDiffCommand.getDiffResult(diffCommandRequest).accept(output);
  public void diffForPathShouldCreateLimitedDiff() throws IOException {
    gitDiffCommand.getDiffResult(diffCommandRequest).accept(output);
  public void diffBetweenTwoBranchesShouldCreateDiff() throws IOException {
    gitDiffCommand.getDiffResult(diffCommandRequest).accept(output);
  public void diffBetweenTwoBranchesForPathShouldCreateLimitedDiff() throws IOException {
    gitDiffCommand.getDiffResult(diffCommandRequest).accept(output);