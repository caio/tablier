---
title: Help
---

# Help

[TOC levels=2-4]

## How to Search

Searching with [gula.recipes][gula] is as simple as typing the things
that you are searching for. We don't try to interpret your search, we
don't exclude words that you typed to "augment" the results, we'll
search for anything you type.

[gula]: https://gula.recipes

If you are looking for recipes with "beans" and "bacon" you can simply
type these words ([see the results live][bb]):

[bb]: /search?q=beans+bacon

    beans bacon

Now if you were looking for recipes with "almond butter" you could
just type the words as before and you would notice that [there are
many results][ab]. That's because we consider every word separately,
so the results contain recipes that have almonds and butter as separate
ingredients, for example.

You can narrow down you search for "almond butter" by grouping the
words:

    "almond butter"

Notice that the quotes are included. This [results in way less matching
recipes][ab2], which makes it easier to find what you're looking for.

Let's say that for some reason you really dislike when people mix
almond butter with chocolate, so now instead of including a word you
want to _exclude_ it. Easy, you type the following as your search:

    "almond butter" -chocolate

And then the results [will not contain recipes with chocolate][abc].
You can go on adding words and phrases that you want to include/exclude
until it matches your preferences.

If, for example, you wanted to find recipes with almonds but excluding things
like "almond butter" and "milk" you would search for:

    almond -"almond butter" -milk

[ab]: /search?q=almond+butter
[ab2]: /search?q=%22almond+butter%22
[abc]: /search?q="almond+butter"+-chocolate

### Filtering and Reordering Results

If you have some restrictions like a particular diet or want to have a
finer grained control of what results you are getting (limiting how
many ingredients you want to use, for example), you should take a look
at the [search sidebar][sidebar].

<div class="notification is-warning">
Note that many recipes don't report their metadata, so if you filter
for calories for example, every recipe that doesn't report their
nutritional data will be <em>excluded</em> from the results.
</div>

You may be in a hurry and want something really quick to make. Then
you choose to only show recipes with up to 5 ingredients, that are ready
in 15 minutes or less and up to 200 kcal: [you got it!][filtered]

[sidebar]: /search?q=almond+-%22almond+butter%22+-milk#sidebar
[filtered]: /search?q=almond+-%22almond+butter%22+-milk&ni=0,5&tt=0,15&n_k=0,200


#### Advanced: Custom Filter Ranges

If you want to further tweak your filtering, you may change the search
parameters directly in your browser's bar. Ranges are encoded as
`start,end` so when you see something like:

    &ni=0,5

In the URL bar, it means that the filter ("ni" stands for Number of
Ingredients in this case) is limiting from 0 to 5. If you change it
to `2,7` the results will change accordingly.

#### Advanced: Extending diet-filtered results

When you restrict you search too much you might end up with too few
results. That isn't a bad thing if you find what you are looking for,
but oftentimes diet restrictions make it very difficult to find
good recipes, especially because we rely on the original authors
flagging that the recipe is valid for a diet.

Resuming our search for recipes super fast to prepare, with almonds
but no milk nor "almond butter" and then restricting it to only
show recipes for the Keto diet, we'll get [very few results][keto].

[keto]: /search?q=almond+-%22almond+butter%22+-milk&ni=0,5&tt=0,15&n_k=0,200&diet=keto

That happens a bit too often, so in order to try to give more recipe
options you can activate our AI by appending the `science` parameter
to the URL query parameter. The parameter takes a number between 0.5
and 1, where this number means the confidence in our diet guessing
AI.

So a parameter like `&science=0.85` means "show me recipes that match
my selected diet **and** recipes that you think could match this diet
with 85% confidence".

Empirically, we see that higher confidence (`0.8` or more) tends to
return recipes that either match perfectly with the selected diet or
are trivial to make it so by replacing/removing an ingredient.

If you compare the [previous results][keto] with the results [augmented
by gula.recipe's AI][science] (Using a threshold of 0.85), you'll see
a few new perfectly matching recipes.

[science]: /search?q=almond+-%22almond+butter%22+-milk&ni=0,5&tt=0,15&n_k=0,200&diet=keto&science=0.85
