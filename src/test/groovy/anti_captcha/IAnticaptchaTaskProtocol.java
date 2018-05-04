package anti_captcha;

import anti_captcha.ApiResponse.TaskResultResponse;

public interface IAnticaptchaTaskProtocol {

    TaskResultResponse.SolutionData getTaskSolution();

}
