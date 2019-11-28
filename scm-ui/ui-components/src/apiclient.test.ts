import { apiClient, createUrl, extractXsrfTokenFromCookie } from "./apiclient";
import fetchMock from "fetch-mock";
import { BackendError } from "./errors";

describe("create url", () => {
  it("should not change absolute urls", () => {
    expect(createUrl("https://www.scm-manager.org")).toBe("https://www.scm-manager.org");
  });

  it("should add prefix for api", () => {
    expect(createUrl("/users")).toBe("/api/v2/users");
    expect(createUrl("users")).toBe("/api/v2/users");
  });
});

describe("error handling tests", () => {
  const earthNotFoundError = {
    transactionId: "42t",
    errorCode: "42e",
    message: "earth not found",
    context: [
      {
        type: "planet",
        id: "earth"
      }
    ]
  };

  afterEach(() => {
    fetchMock.reset();
    fetchMock.restore();
  });

  it("should create a normal error, if the content type is not scmm-error", done => {
    fetchMock.getOnce("/api/v2/error", {
      status: 404
    });

    apiClient.get("/error").catch((err: Error) => {
      expect(err.name).toEqual("Error");
      expect(err.message).toContain("404");
      done();
    });
  });

  it("should create an backend error, if the content type is scmm-error", done => {
    fetchMock.getOnce("/api/v2/error", {
      status: 404,
      headers: {
        "Content-Type": "application/vnd.scmm-error+json;v=2"
      },
      body: earthNotFoundError
    });

    apiClient.get("/error").catch((err: BackendError) => {
      expect(err).toBeInstanceOf(BackendError);

      expect(err.message).toEqual("earth not found");
      expect(err.statusCode).toBe(404);

      expect(err.transactionId).toEqual("42t");
      expect(err.errorCode).toEqual("42e");
      expect(err.context).toEqual([
        {
          type: "planet",
          id: "earth"
        }
      ]);
      done();
    });
  });
});

describe("extract xsrf token", () => {
  it("should return undefined if no cookie exists", () => {
    const token = extractXsrfTokenFromCookie(undefined);
    expect(token).toBeUndefined();
  });

  it("should return undefined without X-Bearer-Token exists", () => {
    const token = extractXsrfTokenFromCookie("a=b; c=d; e=f");
    expect(token).toBeUndefined();
  });

  it("should return xsrf token", () => {
    const cookie =
      "a=b; X-Bearer-Token=eyJhbGciOiJIUzI1NiJ9.eyJ4c3JmIjoiYjE0NDRmNWEtOWI5Mi00ZDA0LWFkMzMtMTAxYjY3MWQ1YTc0Iiwic3ViIjoic2NtYWRtaW4iLCJqdGkiOiI2RFJpQVphNWwxIiwiaWF0IjoxNTc0MDcyNDQ4LCJleHAiOjE1NzQwNzYwNDgsInNjbS1tYW5hZ2VyLnJlZnJlc2hFeHBpcmF0aW9uIjoxNTc0MTE1NjQ4OTU5LCJzY20tbWFuYWdlci5wYXJlbnRUb2tlbklkIjoiNkRSaUFaYTVsMSJ9.VUJtKeWUn3xtHCEbG51r7ceXZ8CF3cmN8J-eb9EDY_U; c=d";
    const token = extractXsrfTokenFromCookie(cookie);
    expect(token).toBe("b1444f5a-9b92-4d04-ad33-101b671d5a74");
  });
});
