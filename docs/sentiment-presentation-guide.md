# Sentiment Analysis: Presentation Guide

## My part

I implemented the sentiment analysis feature. It looks at each retrieved post and labels it as happy (`:-)`), sad (`:-(`), or neutral (`:-|`). It checks for recognized English and Portuguese words, emojis, and emoticons. It also handles some nearby negation, such as “not good.”

The classifier requires at least two sentiment cues. If happy or sad cues make up more than 70% of the cues, it returns that sentiment; otherwise, it returns neutral. The sentiment is saved with the post and displayed on its result card.

The main files for my feature are:

- `SentimentAnalysisService.java` and `SentimentAnalysisServiceImpl.java`: define and perform the classification.
- `MastodonServiceImpl.java`: call the classifier when turning API responses into posts.
- `Post.java`: store the sentiment result.
- `search.scala.html`: display the sentiment badge.
- `SentimentAnalysisServiceImplTest.java`: test classification cases.

## How the whole app works

1. The user enters a hashtag in the browser and submits the search form.
2. `HomeController` checks the hashtag and asks `MastodonService` for posts.
3. `MastodonService` checks the cache. If the results are not cached, `MastodonClient` requests them from the Mastodon REST API.
4. The service turns the API response into post objects. It cleans the post HTML, calculates sentiment, and saves the result with each post.
5. The controller asks the statistics service to summarize the posts, then the search page displays the posts, sentiment badges, and summaries.
6. Other services provide word statistics and author analysis when the user opens those features. Pagination requests another page of posts.

In short, `HomeController` coordinates the request, while the services each handle a specific task. The page displays the results produced by those services.

## Short presentation script

> My contribution was the sentiment analysis feature. For each post returned by Mastodon, the application checks its readable text for happy and sad words, emojis, and emoticons. It accounts for some nearby negation and applies a 70% rule to choose happy, sad, or neutral. The result is stored on the post and shown as a badge on the search results page.
>
> Overall, the controller coordinates the search. The Mastodon service gets and prepares posts, and separate services calculate sentiment and other summaries. The view then presents those results to the user.
