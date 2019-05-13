---
title: About
---

# About

*Gula* is the Portuguese name for gluttony; [gula.recipes][gula] is the
Private, Ad-Free Recipe Search Experience. It's brought to you by
[Caio Romão][me] and the [code is open source][code].

[gula]: https://gula.recipes
[me]: https://caio.co
[code]: https://github.com/gula-recipes

## Not a Business

This was created and is still developed as a hobby project, meaning
that in here there's a lot of perceived value in having fun, pacing
myself, keeping things lean and sticking to my ideals.

Sustainability is a goal, profit isn't. Traditional ads will never have
a place here, which means there's no incentive to making things
inefficient: I want you to be able to find a recipe and get to cooking
as quickly as possible, not keep you using this site for long just so
that I can show you a lot of ads.

## History

Autumn 2017, I decide to fix some of my bad habits and engaged in
cooking mostly low-carb meals daily and using [r/progresspics][pp]
and similar as the main weight loss motivation driver.

[pp]: https://www.reddit.com/r/progresspics/

While the weight was going down, a series of frustrations with the
status-quo of searching for recipes started building up: pervasive
tracking, ads everywhere, excessive interactivity, subscription walls,
rubbish generated content to drive traffic and slow data-heavy pages.

A year and 50+ kg (110 lbs) lost later, I found myself wanting to give
back to the community, but unable to do so since sharing my success
story via before/after pictures triggered too many concerns related to
privacy and misuse (and self confidence, of course).

Meanwhile, I started writing a simple tool to help me manage the
somewhat big list of recipe links I had collected over time:
A plain-text file wasn't good enough anymore, the existing websites
offering a similar thing hadn't improved on my gripes over time (quite
the opposite).

I quickly found myself writing scrapers for my favorite sites to make
it easier to extract the recipes out of the wall of text that usually
precedes the real content. After a few iterations of doing just that,
things started clicking and I went from a small hand-crafted database
of about 100 recipes to 10.000 recipes with little effort; A few more
tweaks and forgetting the crawler running overnight left me with
around 100.000 recipes.

And then things got interesting :-) Growing this to the current 1+M
database size took more time than effort and I finally had something
that I could do to keep me entertained for a while _and_ give back to
the community: a recipe search engine that doesn't have all of those
annoying things we all have to deal with in today's web.

# Acknowledgments

This project couldn't have been made at a reasonable pace without
the help of open source and/or freely distributed packages. Some
highlights are:

* The SVG icons come from the [IcoMoon](https://icomoon.io/) free icon
  pack, licensed as [CC BY 4.0][cc]
* The [Bulma CSS Framework](https://bulma.io/) made my life a lot easier
  since front-end technologies are not my forte
* [Lucene](https://lucene.apache.org/core/) powers the search
  functionality and reading its code always teaches me a _lot_.
* Every open source library and tool used by the subprojects in the
  [gula.recipes organization][code].

[cc]: https://creativecommons.org/licenses/by/4.0/
