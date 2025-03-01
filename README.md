# ♕ BYU CS 240 Chess

This project demonstrates mastery of proper software design, client/server architecture, networking using HTTP and WebSocket, database persistence, unit testing, serialization, and security.

## 10k Architecture Overview

The application implements a multiplayer chess server and a command line chess client.

[![Sequence Diagram](10k-architecture.png)](https://sequencediagram.org/index.html#initialData=C4S2BsFMAIGEAtIGckCh0AcCGAnUBjEbAO2DnBElIEZVs8RCSzYKrgAmO3AorU6AGVIOAG4jUAEyzAsAIyxIYAERnzFkdKgrFIuaKlaUa0ALQA+ISPE4AXNABWAexDFoAcywBbTcLEizS1VZBSVbbVc9HGgnADNYiN19QzZSDkCrfztHFzdPH1Q-Gwzg9TDEqJj4iuSjdmoMopF7LywAaxgvJ3FC6wCLaFLQyHCdSriEseSm6NMBurT7AFcMaWAYOSdcSRTjTka+7NaO6C6emZK1YdHI-Qma6N6ss3nU4Gpl1ZkNrZwdhfeByy9hwyBA7mIT2KAyGGhuSWi9wuc0sAI49nyMG6ElQQA)

[Sequence Diageam](https://sequencediagram.org/index.html?presentationMode=readOnly#initialData=IYYwLg9gTgBAwgGwJYFMB2YBQAHYUxIhK4YwDKKUAbpTngUSWDABLBoAmCtu+hx7ZhWqEUdPo0EwAIsDDAAgiBAoAzqswc5wAEbBVKGBx2ZM6MFACeq3ETQBzGAAYAdAE5M9qBACu2GADEaMBUljAASij2SKoWckgQaIEA7gAWSGBiMPDI5jAAtAB85JQ0UABcMADaAAoA8mQAKgC6MAD0PgZQADpoAN4ARJ2UwQC2KAPlAzADADQzuOrJ0ByT03MzKKPASAhrMwC+mNnCZQXFbJzcFTCDw1BjE1Mz8wOLqstQq88bA1s7ex+R2yly4lHOJREKkqUCiMUyUAAFJForFKJEAI4+NRgACUx0hVFEENk8iUKnUlXsKDAAFUuoj7o98dlSYplGpVBDjOUAGJITgwemUNkwHRhJnAcYE4AIZjCh5SwwhWVIHTcAms7TkzkFIqE0SVNA+BAITUGlQk7UcykwECwuQoBWMrpslnZGTWilc-U8hQcDhC13ac2nYn6tk6232lCOhQ+MCpRHABOpN3myM2n3FHkwf2B+OJtmh0rh-Wg66VFHw9FqE1YD2sdhg2D5fWnSiVO5dR7TSrrV4pxONCAAa3QfZmhxL1HBbeKiFQGEqACYnE5et2RkrJwOZkPUiPx2hd9PsigEAYg9vxjB5MeM17dfOLShKvdi42w5aXxXO9fFXGRpgGPABRAAPFRsAIRJzT-Vt21Kf8+hmcZ1GAalJhmUCoG8G5ZQdDgwnvCcYGBD0OwQhdcmXGAABYnAAZk3VDOQwp5sNw6BKgI2MiLvEDSPI9AOBlOUYAAWRiVQBUcSVb2gGB3k+QNFP+XY4Oba49WKSiu1Y9DMP7HC8MqPRA1hLEcWmciTiQqiciXMBKiqBinGaFiBjQ1R2KwgYTO4sVgAslArNiGyCREsTmDqRNwQFBFggQGBKDwmciR-cstP-WLUnBCCoJgtBNKuOdENnG4UK8tijM40yYERDg1HtYgipgCAADMUq4qBcQir97J0xzzBcgBWdcPP6AyfNq-yesqRrmqgVqEiSTrurwvqyMizgzF2zxvD8QJoHYakYAAGQgaIkgCNIMjERc8nKsoXPqJpWjaAx1FWzd5JQeZlJWI5KIheD9KGHsdx+V5Aa+fYIvgiFvzfGAECugVEUu67MWxWJ8WRq0ySzKkaWdP6Af0D4VnxTNvW5HRKn5QUFVFcUYD+mBmyUymVMwWV5S6GAOt8XatSJumX2RyoWZDOyoUMIpac5SoYzjVMXRvFB3SV9R6cqfM81TT9XyGsGLvRtBIlUesCURyX7PBv74cHVMjwnH5trls4X0e2i1w3KaIc15391dsd3fWbaLyvBUYDQCBmGFnxRc9cXn2ew045NM0vbLC5spuWlglTaAkAALxQDgCpQaDVttguhr025pt84z5vZ4vE1LivVk9wkyuopyXIYgBGSaqu81u6sC5OD27yuIqi-mYFy+KMG3ZLUugdK86bUqblX2Bq9r2DGzt57kJb2aApuRbVBak+1q6rfev6ijBp9mjnOqcb3M8yfr7tzvg-Nq60X5bWErtKKB1fD+ACF4FA6Bzb2F8MwW66RMgEl9swC+NwqjSFAudUCjRQIfS+jJRIvQDxu2Kl7Oc+d95dgGHPZaZd4iJDWNQ8OJ5yJ210qWaEqMrqoMxsIhMOMcTugJhGJ8tomrcEyIWJMXDjza1kdmIwDMZAXhpIYJRrMwgqPQNFQ2RZtBGB0ZkUSjYdY+gzoIiw2Id6ZQYS2SoVAIBIGsR6c+-CKpdlsv3By2CXL+3HsJS8eijbmPjsweRujvFi3ZBLexKMOqygMM4hWWVGFCk7qkeeVdII1yKiVFsjcHbN2qoZDic16qzxLqwhefc6HBK-sPJwY9-41VqTfd8+TClvyitkZeh8YAJQ3htbejZc4uL3m4qpADelAKavfZaj92rPx6hA80IIG7238Ysnpfk+kNVWSA1amypmvxae-CqQ0Qk-wmt0mpJyVlLRWokK54ChlQP2l4WBgRYSBnOvCGAABxJUXJ0H3SwV-JGlSqjguIR9ewSoqFhwfK00GBcmEsPLuwk8UwjE8PrvvBF8tKjIFiJCtCmN4QSLxgSaRxRbEkzAPo7QyZMXoDUWnXWvotHgppKYtM5i2YkpMZy+QQsRaJNTsk9OfiMoo2lcALJhNFW2mpWAWlahETuiSVGDRuZhXMFlMlNFaFHz8rscqzOerQWxA1b+XFF0GV1jlGU7SByXpVKtWoLCVRBgBoAJLSCwiPFcjE6KvDupkBUvZoa9AGOqUAo5E1Qz3AMANAA5JUQIYDNECSDT+Q9qhhM8gG1QQaQ1KnDZG6NsaZjxqdJDcYzsU1ppABm9tTxs15oLesA4RaIlXjVXHBOsrk7ytsRUylWdTQupyQsou+Ke7H1KWffZuDwZLLefUgZTTe4lo-vqR5rlOnjyvssw967mmQO8aMuKsAJmKk3j1XZJtXW5M3XXGZ8yfW7qOa8tu9VgHrNAVszab8Br3LLSNJ5f9A77rA4FCDnyn7XJ2eeP5okYFHQCNgHwUBsDcHgA6TIEKlQpAwVkYapBgO1AaC0doAaMXDm4ZuQd4xyKltcZWW4zDGkEqKpwnlpK9nktSSrSjKA9WIjgHJxleJmUCIVqy9R7K1Xcs46ogkc7BWVDNaKgxnMJNSuiTKpOKcjVZnnSq-WVn1VwZVZq41snYyZAU2G6Q8xjSmlmL0ALCB5g8f+jAANhqFXGr1hRrzhg9WimSBkVIkX61+cXaF4L2cwtKnzeMPL0pXO7zNkphLVsbbbuk8BqqvnG0xtuaW897SK3PMDvVyoUbGtjqiWYmVsTp22Zi-Z31mcQvLoE-+NdImN3FMft6geQS93HLQzcBpXdj2waCQ81rl6ukodW9PdbR7y4Pp2k+8SUlvoOHS+MAr9GpPlN9ZfapM1b2BXMjASyuMwDbea4PRDl7kMTyO3Uz7wVvuhV+78y7MUX3jPXu+65X7pErsE3+0+MzfHLZA+9g96HzmQcuWA7Z23sUIdolUX+163tT3B7fInmHvlk77lFaBALCOWAvGjZIMAABSnikh6sCN20ccKnIUr9bUWkrG2jsfyTQzcZHgDc6gHACAaMoBFZQOGvjH8puVQWAgVXlANda7WAAdRYKGkhbQABC50FBwAANK-E6zAbrdE+58NfJUAAVkLhTguBQqakep9zxNIs0h0ySvlWqTVCpFROiVFmRniQnTZ2d6iHOZzVS6zTtr2U+Yy9FwzOYk-MCS+KsIvnzTL2r9ZuVX7DMyeo+MY2jZl6traGLmAABefvWWv2zI0yN7075sBaG80qREvn5g97F-3v60WbE56M0KKfjoYCtszeMXvJue178ML0EA0BYQ2ESBwWSd4IB3d19IW-7M+0j+-ej-8HivGv5x03PogTXNLYXqVr9D-4pSRI750YH7powAACEQ+E2AGo+psbq5u0AwEYE82W6iBP+lSoOoGx2PECAhExEgkJ4tyABbS5al6jEtOqGBBnMRBfEJBx45OKUw20ciWNGg2WeLe6+beCBMyaOhuxm+WCcvIcqmOtC2OO6yqr2dBDOZkkOP21k5BO2lO38wOtBYOpyX2yh4UtywyYB46zmk6iczea+tquegiAhSBP6q6p2c2hU-6PiMhuOeB+Oa2-S96J66UgBe2o8Wh+BChHc3hsOJi12Mkt2KuauqBsAikde1Wz2tWN6BONwuh0OKhp68GLWVBbkgRHh9B6RYUf2bOKcz6eUr6SOSUKOAGQhgG-4khX6OBhy7h9OpyGGGypOMGqhAODGGhNOLyBRwRHRUG2GYRHOh0cCXgquOQsYsAwA2AZGhAhKtGsK2Q2CUu-4+ChCxCpC7Qxgam8GwhtwvC+y9qgiIA3AeABqhxbmMiRedoVxSI8esWG+0gliyopobBFgqAGgdmKS5xKMn+3idRZswJZKSRshlU+u2RgOVOwBf+JiYyb61RL8X6qOEedhGOmBzhiRQGUJK2QR7RTOnR0G0AOGghZ6cJ-R7WrRgC4GJJoxPypRokUUQAA)

## Modules

The application has three modules.

- **Client**: The command line program used to play a game of chess over the network.
- **Server**: The command line program that listens for network requests from the client and manages users and games.
- **Shared**: Code that is used by both the client and the server. This includes the rules of chess and tracking the state of a game.

## Starter Code

As you create your chess application you will move through specific phases of development. This starts with implementing the moves of chess and finishes with sending game moves over the network between your client and server. You will start each phase by copying course provided [starter-code](starter-code/) for that phase into the source code of the project. Do not copy a phases' starter code before you are ready to begin work on that phase.

## IntelliJ Support

Open the project directory in IntelliJ in order to develop, run, and debug your code using an IDE.

## Maven Support

You can use the following commands to build, test, package, and run your code.

| Command                    | Description                                     |
| -------------------------- | ----------------------------------------------- |
| `mvn compile`              | Builds the code                                 |
| `mvn package`              | Run the tests and build an Uber jar file        |
| `mvn package -DskipTests`  | Build an Uber jar file                          |
| `mvn install`              | Installs the packages into the local repository |
| `mvn test`                 | Run all the tests                               |
| `mvn -pl shared test`      | Run all the shared tests                        |
| `mvn -pl client exec:java` | Build and run the client `Main`                 |
| `mvn -pl server exec:java` | Build and run the server `Main`                 |

These commands are configured by the `pom.xml` (Project Object Model) files. There is a POM file in the root of the project, and one in each of the modules. The root POM defines any global dependencies and references the module POM files.

## Running the program using Java

Once you have compiled your project into an uber jar, you can execute it with the following command.

```sh
java -jar client/target/client-jar-with-dependencies.jar

♕ 240 Chess Client: chess.ChessPiece@7852e922
```
