This page provides markup guidance on [void elements](https://html.spec.whatwg.org/multipage/syntax.html#void-elements) in HTML documents — that is, the [`area`](https://html.spec.whatwg.org/multipage/image-maps.html#the-area-element), [`base`](https://html.spec.whatwg.org/multipage/semantics.html#the-base-element), [`br`](https://html.spec.whatwg.org/multipage/text-level-semantics.html#the-br-element), [`col`](https://html.spec.whatwg.org/multipage/tables.html#the-col-element), [`embed`](https://html.spec.whatwg.org/multipage/iframe-embed-object.html#the-embed-element), [`hr`](https://html.spec.whatwg.org/multipage/grouping-content.html#the-hr-element), [`img`](https://html.spec.whatwg.org/multipage/embedded-content.html#the-img-element), [`input`](https://html.spec.whatwg.org/multipage/input.html#the-input-element), [`link`](https://html.spec.whatwg.org/multipage/semantics.html#the-link-element), [`meta`](https://html.spec.whatwg.org/multipage/semantics.html#the-meta-element), [`source`](https://html.spec.whatwg.org/multipage/embedded-content.html#the-source-element), and [`track`](https://html.spec.whatwg.org/multipage/media.html#the-track-element), [`wbr`](https://html.spec.whatwg.org/multipage/text-level-semantics.html#the-wbr-element) elements — while explaining the context for the checker _“Trailing slash on void elements has no effect and interacts badly with unquoted attribute values”_ message.

## Trailing slashes

In the HTML Standard, the [section defining requirements for the start tag](https://html.spec.whatwg.org/multipage/syntax.html#start-tags) of an element includes the following paragraph:

> If the element is one of the [void elements](https://html.spec.whatwg.org/multipage/syntax.html#void-elements), or if the element is a [foreign element](https://html.spec.whatwg.org/multipage/syntax.html#foreign-elements), then there may be a single U+002F SOLIDUS character (`/`), which on [foreign elements](https://html.spec.whatwg.org/multipage/syntax.html#foreign-elements) marks the start tag as self-closing. On [void elements](https://html.spec.whatwg.org/multipage/syntax.html#void-elements), it does not mark the start tag as self-closing but instead is unnecessary and has no effect of any kind. For such void elements, it should be used only with caution — especially since, if directly preceded by an [unquoted attribute value](https://html.spec.whatwg.org/multipage/syntax.html#unquoted), it becomes part of the attribute value rather than being discarded by the parser.

Note, in particular, the standard states the following two points about trailing slashes (`/` U+002F SOLIDUS characters):

- Trailing slashes in void-element start tags do not mark the start tags as self-closing.
- Trailing slashes directly preceded by [unquoted attribute values](https://html.spec.whatwg.org/multipage/syntax.html#unquoted) become part of the attribute value.

The following sections provide a few more details about those two points.

### Trailing slashes in void-element start tags do not mark the start tags as self-closing

What the [quoted paragraph above from the HTML standard](#trailing-slashes) is saying is that, to mark up, for example, an [`hr`](https://html.spec.whatwg.org/multipage/grouping-content.html#the-hr-element) element, both of the following are allowed:

- Start tag without trailing slash:

  ```html
  <hr>
  ```

- Start tag with trailing slash:

  ```html
  <hr/>
  ```

However, what the standard also explicitly states is that in the `<hr/>` case, the trailing slash **does not mark the start tag as self-closing**.

In other words, the standard states that you can use a trailing slash in a void-element start tag for literally any purpose _except_ to mark the start tag as self-closing.

So, for example, the following are all acceptable reasons for using a trailing slash in a void-element start tag —

- ✅ I use a trailing slash because I like how it looks.
- ✅ I use a trailing slash because I run my HTML markup through a formatting tool that’s hardcoded to add trailing slashes to all void-element start tags, without any option for me to prevent the tool from doing that.
- ✅ I use a trailing slash because I write a lot of JSX code, and JSX requires the trailing slash — without any option for me to prevent JSX from doing that — so for consistency with what I’m accustomed to in JSX, I follow that same style in actual HTML documents.

However, the following reason **does not** concur with what the HTML standard states —

- ❌ I use a trailing slash to mark start tags as self-closing.

For more about this, see the [Promoting the right mental model for void elements](#promoting-the-right-mental-model-for-void-elements) section.

### Trailing slashes directly preceded by unquoted attribute values

Consider the following two cases for marking up an `img` element for the image at `http://www.example.com/logo.svg`:

- `img` element without trailing slash:

  ```html
  <img alt=SVG src=http://www.example.com/logo.svg>
  ```

- `img` element with trailing slash:

  ```html
  <img alt=SVG src=http://www.example.com/logo.svg/>
  ```

In the first case (without the trailing slash), `http://www.example.com/logo.svg` becomes the value of the `src` attribute in the DOM, as expected.

However, in the second case (with the trailing slash), `http://www.example.com/logo.svg/` unexpectedly becomes the value of the `src` attribute in the DOM — which breaks display of the image. See https://software.hixie.ch/utilities/js/live-dom-viewer/?saved=10856 for a live demo of that problem.

And that’s why the HTML standard states that trailing slashes should be used only with caution — to avoid cases like the one above, where the trailing slash can unexpectedly become part of an attribute value.

### Configuring tools to not output trailing slashes for void elements

This section provides how-to info on configuring particular tools to not add trailing slashes to void elements.

- [Prettier](https://prettier.io/) is hardcoded to add trailing slashes to all void-element start tags, and provides no option for users who want the choice of not having trailing slashes in void elements — though there is a [highly-upvoted issue](https://github.com/prettier/prettier/issues/5246) that’s been open since 2018 asking for such an option. There is a [community-maintained plugin](https://github.com/awmottaz/prettier-plugin-void-html) to not use trailing slashes in void elements.

- [VS Code](https://code.visualstudio.com/), with the Prettier plugin installed, adds trailing slashes to all void elements. But [you can prevent that](https://stackoverflow.com/questions/50261161/how-do-i-stop-prettier-from-formatting-html-files/56061737#56061737) — by adding the following to your `settings.json` file:

  ```
  "editor.formatOnSave": true,
  "[html]": {
      "editor.defaultFormatter": "vscode.html-language-features"
  }
  ```

- [Autoptimize](https://wordpress.org/plugins/autoptimize/) can be [configured](https://wordpress.org/support/topic/plz-add-a-filter-to-htmlminify/) to output void elements without trailing slashes, by using a filter with the [`autoptimize_html_after_minify`](https://wp-plugin-api.com/hook/autoptimize_html_after_minify/) hook — like this:

  ```php
  add_filter( 'autoptimize_html_after_minify', function( $html ) {
      return str_replace( '/>', '>', $html );
  } );
  ```

- [Joomla](https://www.joomla.org/) adds trailing slashes to void elements output by, for example, `$doc->addHeadLink` — but there’s [an open issue](https://github.com/prettier/prettier/issues/5246) asking for that behavior to be made optional.

- [Symfony](https://symfony.com/) adds trailing slashes to [`input`](https://html.spec.whatwg.org/multipage/input.html#the-input-element) elements — but there’s [an open issue](https://github.com/symfony/symfony/pull/47715) suggesting that be changed.

- [Hugo](https://gohugo.io/) has a `{{ hugo.Generator }}` call which causes a [`meta`](https://html.spec.whatwg.org/multipage/semantics.html#the-meta-element) element to be output with a trailing slash. To avoid that, rather than using that call, you can instead use this HTML markup:

  ```html
   <meta name="generator" content="Hugo {{ hugo.Version }}">
   ```

- [Umbraco Forms](https://umbraco.com/products/umbraco-forms/) adds trailing slashes to [`input`](https://html.spec.whatwg.org/multipage/input.html#the-input-element) elements; a [related issue](https://github.com/umbraco/Umbraco.Forms.Issues/issues/889) was closed without a fix, with this comment:
   > there is as discussed the option of taking copies of the view files, saving them on disk in the expected locations, and updating them to your preference. For Forms 10, the default theme views are shipped as part of a Razor Class Library, but you can get copies of them for reference/adapting [via the documentation](https://our.umbraco.com/documentation/add-ons/umbracoforms/developer/Themes/#creating-a-theme).

- [ASP.NET Core](https://dotnet.microsoft.com/en-us/apps/aspnet) has a mechanism for automatically adding a CSRF token to a document, and that mechanism creates an [`input`](https://html.spec.whatwg.org/multipage/input.html#the-input-element) element with a trailing slash; a [related issue](https://github.com/dotnet/aspnetcore/issues/44341) was closed without a fix.

### Suppressing the “Trailing slash on void elements has no effect” message

If you want to persistently suppress/filter/ignore the _“Trailing slash on void elements has no effect”_ messages from the checker, you can use the <kbd>Message Filtering</kbd> button in the checker UI:

![message-filtering](https://user-images.githubusercontent.com/194984/194275098-48be2da4-fe64-4d5a-8f9e-fc044e26cad4.gif)

After you do that, the _“Trailing slash on void elements has no effect”_ messages will be persistently filtered out for all future documents you check.

When using the checker from the command line, you can suppress/filter/ignore the _“Trailing slash on void elements has no effect”_ messages using the [`--filterfile` or `--filterpattern` command-line options](https://github.com/validator/validator/wiki/Message-filtering#you-can-filter-messages-with-the-command-line-checker).

And when running your own instance of the checker service, you can suppress/filter/ignore the _“Trailing slash on void elements has no effect”_ messages using one of the [several different mechanisms provided](https://github.com/validator/validator/wiki/Message-filtering#you-can-filter-messages-globally-for-your-own-checker-service).

## Promoting the right mental model for void elements

When learning about HTML, teaching about HTML, and designing HTML tools, it’s important to have and promote the right mental model about how HTML actually works.

And the way HTML works is that there’s [a discrete set of void elements](https://html.spec.whatwg.org/multipage/syntax.html#void-elements), hardcoded into HTML parsers, and people who write HTML must learn and remember which elements those are — without any trailing slash needing to be added to those elements to indicate they are special.

Using trailing slashes in void elements (and teaching others to use trailing slashes, and making your tools output trailing slashes) promotes a misleading mental model of HTML where the trailing slash _looks_ like it has some special significance — when in fact, as [the HTML standard says](https://html.spec.whatwg.org/multipage/syntax.html#start-tags), it’s _“unnecessary and has no effect of any kind”_.

For more explanation, see the following:

- https://github.com/orgs/mdn/discussions/242#discussioncomment-3749398
- https://github.com/orgs/mdn/discussions/242#discussioncomment-3759664
