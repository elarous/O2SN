# O2SN (Open Source News Social Network)

### Intro

This project is intended for helping me to learn Clojure and Clojurescript and get familiar with their ecosystem, especially for web development.

### The Idea

It's about a website, on which you can share news and get news by locations, but instead of doing that the old way (just sharing and retrieving) i thought about a simple rating system; everyone can share stories, but those stories can be marked by users as truth or lie, so the story rating depends on : 1) the number of truths and lies given. 2) the credibility of user rated the story. 3) the distance between that user and the story location.
this means that only people near the event should rate it, it's not just because they have a bigger impact on the story's rating, but because giving ratings updates the user's credibility too.
A user can subscribe to channels, each channel represents one location (i used the google map's locations levels: country, admin-level-1,admin-level-2 and locality)
Users can like or dislike a story, which doesn't effect the story's rating nor the user's credibility

### Features
- [x] A user can signup for an account
- [x] A user can login and logout to his account
- [x] A user can share a new story 
- [x] A user can mark a story as truth or lie
- [x] A user can like or dislike a story
- [x] A user can view a story in details
- [x] A user can subscribe an unsubscribe to a location's channel
- [x] A user can load stories of a channel and sort them
- [x] A user can search for a story or a user
- [x] A user have a profile
- [x] A user can see other users profiles
- [x] A user receives notifications about relevant new stories or actions
- [ ] A user can change his profile
- [ ] A user can follow other users
- [ ] A user can message other users
- [ ] A user can report a story or a user
- [ ] A user can request another user to merge their stories if they are about the same event
- [ ] A user can customize settings

## Screenshots
### home page
#### Default home page
![default home page]("screenshots/1.png")
#### Selecting a channel
![selecting a channel]("screenshots/2.png")
#### Loading Stories
![loading stories]("screenshots/3.png")
#### Clicking a story card to reveal actions
![clicking a story card to reveal actions]("screenshots/4.png")
#### View users who likes or dislikes the story
![view users who likes or dislikes the story]("screenshots/5.png")




