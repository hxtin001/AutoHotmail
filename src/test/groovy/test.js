var Anticaptcha = function(clientKey, usePrecaching) {
    return new function(clientKey, usePrecaching) {
        usePrecaching = !!usePrecaching;
        this.params = {
            host: "api.anti-captcha.com",
            port: 80,
            clientKey: "f8228269a43caf2e25ebfafef82515dd",
            websiteUrl: null,
            websiteKey: null,
            websiteSToken: null,
            proxyType: "http",
            proxyAddress: null,
            proxyPort: null,
            proxyLogin: null,
            proxyPassword: null,
            userAgent: "",
            cookies: "",
            websitePublicKey: null,
            phrase: null,
            case: null,
            numeric: null,
            math: null,
            minLength: null,
            maxLength: null,
            imageUrl: null,
            assignment: null,
            forms: null,
            softId: null,
            languagePool: null
        };
        var connectionTimeout = 20,
            firstAttemptWaitingInterval = 5,
            normalWaitingInterval = 2;
        this.getBalance = function(cb) {
            var postData = {
                clientKey: this.params.clientKey
            };
            this.jsonPostRequest("getBalance", postData, function(err, jsonResult) {
                if (err) {
                    return cb(err, null, jsonResult)
                }
                cb(null, jsonResult.balance, jsonResult)
            })
        };
        this.createTask = function(cb, type, taskData) {
            type = typeof type == "undefined" ? "NoCaptchaTask" : type;
            var taskPostData = this.getPostData(type);
            taskPostData.type = type;
            if (typeof taskData == "object") {
                for (var i in taskData) {
                    taskPostData[i] = taskData[i]
                }
            }
            var postData = {
                clientKey: this.params.clientKey,
                task: taskPostData,
                softId: this.params.softId !== null ? this.params.softId : 0
            };
            if (this.params.languagePool !== null) {
                postData.languagePool = this.params.languagePool
            }
            this.jsonPostRequest("createTask", postData, function(err, jsonResult) {
                if (err) {
                    return cb(err, null, jsonResult)
                }
                var taskId = jsonResult.taskId;
                cb(null, taskId, jsonResult)
            })
        };
        this.createTaskProxyless = function(cb) {
            this.createTask(cb, "NoCaptchaTaskProxyless")
        };
        this.createFunCaptchaTask = function(cb) {
            this.createTask(cb, "FunCaptchaTask")
        };
        this.createFunCaptchaTaskProxyless = function(cb) {
            this.createTask(cb, "FunCaptchaTaskProxyless")
        };
        this.createImageToTextTask = function(taskData, cb) {
            this.createTask(cb, "ImageToTextTask", taskData)
        };
        this.createCustomCaptchaTask = function(cb) {
            this.createTask(cb, "CustomCaptchaTask")
        };
        this.getTaskRawResult = function(jsonResult) {
            if (typeof jsonResult.solution.gRecaptchaResponse != "undefined") {
                return jsonResult.solution.gRecaptchaResponse
            } else if (typeof jsonResult.solution.token != "undefined") {
                return jsonResult.solution.token
            } else if (typeof jsonResult.solution.answers != "undefined") {
                return jsonResult.solution.answers
            } else {
                return jsonResult.solution.text
            }
        };
        this.getTaskSolution = function(taskId, cb, currentAttempt, tickCb) {
            currentAttempt = currentAttempt || 0;
            var postData = {
                clientKey: this.params.clientKey,
                taskId: taskId
            };
            var waitingInterval;
            if (currentAttempt == 0) {
                waitingInterval = firstAttemptWaitingInterval
            } else {
                waitingInterval = normalWaitingInterval
            }
            if (usePrecaching) {
                waitingInterval = 1
            }
            console.log("Waiting %s seconds", waitingInterval);
            var that = this;
            setTimeout(function() {
                that.jsonPostRequest("getTaskResult", postData, function(err, jsonResult) {
                    if (err) {
                        return cb(err, null, jsonResult)
                    }
                    if (jsonResult.status == "processing") {
                        if (tickCb) {
                            tickCb()
                        }
                        return that.getTaskSolution(taskId, cb, currentAttempt + 1, tickCb)
                    } else if (jsonResult.status == "ready") {
                        return cb(null, that.getTaskRawResult(jsonResult), jsonResult)
                    }
                })
            }, waitingInterval * 1e3)
        };
        this.getPostData = function(type) {
            switch (type) {
                case "CustomCaptchaTask":
                    return {
                        imageUrl: this.params.imageUrl,
                        assignment: this.params.assignment,
                        forms: this.params.forms
                    };
                case "ImageToTextTask":
                    return {
                        phrase: this.params.phrase,
                        case: this.params.case,
                        numeric: this.params.numeric,
                        math: this.params.math,
                        minLength: this.params.minLength,
                        maxLength: this.params.maxLength
                    };
                    break;
                case "NoCaptchaTaskProxyless":
                    return {
                        websiteURL: this.params.websiteUrl,
                        websiteKey: this.params.websiteKey,
                        websiteSToken: this.params.websiteSToken
                    };
                    break;
                case "FunCaptchaTask":
                    return {
                        websiteURL: this.params.websiteUrl,
                        websitePublicKey: this.params.websitePublicKey,
                        proxyType: this.params.proxyType,
                        proxyAddress: this.params.proxyAddress,
                        proxyPort: this.params.proxyPort,
                        proxyLogin: this.params.proxyLogin,
                        proxyPassword: this.params.proxyPassword,
                        userAgent: this.params.userAgent,
                        cookies: this.params.cookies
                    };
                    break;
                case "FunCaptchaTaskProxyless":
                    return {
                        websiteURL: this.params.websiteUrl,
                        websitePublicKey: this.params.websitePublicKey
                    };
                default:
                    return {
                        websiteURL: this.params.websiteUrl,
                        websiteKey: this.params.websiteKey,
                        websiteSToken: this.params.websiteSToken,
                        proxyType: this.params.proxyType,
                        proxyAddress: this.params.proxyAddress,
                        proxyPort: this.params.proxyPort,
                        proxyLogin: this.params.proxyLogin,
                        proxyPassword: this.params.proxyPassword,
                        userAgent: this.params.userAgent,
                        cookies: this.params.cookies
                    }
            }
        };
        this.jsonPostRequest = function(methodName, postData, cb) {
            if (!usePrecaching) {
                if (typeof process === "object" && typeof require === "function") {
                    var http = require("http");
                    var options = {
                        hostname: this.params.host,
                        port: this.params.port,
                        path: "/" + methodName,
                        method: "POST",
                        headers: {
                            "accept-encoding": "gzip,deflate",
                            "content-type": "application/json; charset=utf-8",
                            accept: "application/json",
                            "content-length": Buffer.byteLength(JSON.stringify(postData))
                        }
                    };
                    var req = http.request(options, function(response) {
                        var str = "";
                        response.on("data", function(chunk) {
                            str += chunk
                        });
                        response.on("end", function() {
                            try {
                                var jsonResult = JSON.parse(str)
                            } catch (err) {
                                return cb(err)
                            }
                            if (jsonResult.errorId) {
                                return cb(new Error(jsonResult.errorDescription, jsonResult.errorCode), jsonResult)
                            }
                            return cb(null, jsonResult)
                        })
                    });
                    req.write(JSON.stringify(postData));
                    req.end();
                    req.setTimeout(connectionTimeout * 1e3);
                    req.on("timeout", function() {
                        console.log("timeout");
                        req.abort()
                    });
                    req.on("error", function(err) {
                        console.log("error");
                        return cb(err)
                    });
                    return req
                } else if ((typeof window !== "undefined" || typeof chrome === "object") && typeof jQuery == "function") {
                    jQuery.ajax((window.location.protocol == "https:" ? "https:" : "http:") + "//" + this.params.host + (window.location.protocol != "https:" ? ":" + this.params.port : "") + "/" + methodName, {
                        method: "POST",
                        data: JSON.stringify(postData),
                        dataType: "json",
                        success: function(jsonResult) {
                            if (jsonResult && jsonResult.errorId) {
                                return cb(new Error(jsonResult.errorDescription, jsonResult.errorCode), jsonResult)
                            }
                            cb(false, jsonResult)
                        },
                        error: function(jqXHR, textStatus, errorThrown) {
                            cb(new Error(textStatus != "error" ? textStatus : "Unknown error, watch console"))
                        }
                    })
                } else {
                    console.error("Application should be run either in NodeJs environment or has jQuery to be included")
                }
            } else {
                chrome.runtime.sendMessage({
                    type: methodName + "PrecachedRecaptcha",
                    postData: postData
                }, function(jsonResult) {
                    if (jsonResult.errorId) {
                        return cb(new Error(jsonResult.errorDescription, jsonResult.errorCode), jsonResult)
                    }
                    return cb(null, jsonResult)
                })
            }
        };
        this.setClientKey = function(value) {
            this.params.clientKey = value
        };
        this.setWebsiteURL = function(value) {
            this.params.websiteUrl = value
        };
        this.setWebsiteKey = function(value) {
            this.params.websiteKey = value
        };
        this.setWebsiteSToken = function(value) {
            this.params.websiteSToken = value
        };
        this.setWebsitePublicKey = function(value) {
            this.params.websitePublicKey = value
        };
        this.setProxyType = function(value) {
            this.params.proxyType = value
        };
        this.setProxyAddress = function(value) {
            this.params.proxyAddress = value
        };
        this.setProxyPort = function(value) {
            this.params.proxyPort = value
        };
        this.setProxyLogin = function(value) {
            this.params.proxyLogin = value
        };
        this.setProxyPassword = function(value) {
            this.params.proxyPassword = value
        };
        this.setUserAgent = function(value) {
            this.params.userAgent = value
        };
        this.setCookies = function(value) {
            this.params.cookies = value
        };
        this.setPhrase = function(value) {
            this.params.phrase = value
        };
        this.setCase = function(value) {
            this.params.case = value
        };
        this.setNumeric = function(value) {
            this.params.numeric = value
        };
        this.setMath = function(value) {
            this.params.math = value
        };
        this.setMinLength = function(value) {
            this.params.minLength = value
        };
        this.setMaxLength = function(value) {
            this.params.maxLength = value
        };
        this.setImageUrl = function(value) {
            this.params.imageUrl = value
        };
        this.setAssignment = function(value) {
            this.params.assignment = value
        };
        this.setForms = function(value) {
            this.params.forms = value
        };
        this.setSoftId = function(value) {
            this.params.softId = value
        };
        this.setLanguagePool = function(value) {
            this.params.languagePool = value
        };
        this.setHost = function(value) {
            this.params.host = value
        };
        this.setPort = function(value) {
            this.params.port = value
        }
    }(clientKey, usePrecaching)
};
if (typeof process === "object" && typeof require === "function") {
    module.exports = Anticaptcha
}