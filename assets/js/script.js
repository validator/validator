document.querySelector("h1").textContent = "An Outsider’s Guide to the W3C";
const replaceOnDocument = (pattern, string, {target = document.body} = {}) => {
  // Handle `string` — see the last section
  [
    target,
    ...target.querySelectorAll("*:not(script):not(noscript):not(style)")
  ].forEach(({childNodes: [...nodes]}) => nodes
    .filter(({nodeType}) => nodeType === Node.TEXT_NODE)
    .forEach((textNode) => textNode.textContent = textNode.textContent.replace(pattern, string)));
};
replaceOnDocument(/\[!IMPORTANT\]/g, "👋 Important: ");
replaceOnDocument(/\[!NOTE\]/g, "👉 Note: ");

anchors.options.placement = 'left';
document.addEventListener('DOMContentLoaded', function(event) { anchors.add(); });

tocbot.init({
  // Where to render the table of contents.
  tocSelector: '.js-toc',
  // Where to grab the headings to build the table of contents.
  contentSelector: '.js-toc-content',
  // Which headings to grab inside of the contentSelector element.
  headingSelector: 'h1, h2, h3, h4',
  // For headings inside relative or absolute positioned containers within content.
  hasInnerContainers: true,
  orderedList: false,
  // Show the entire table of contents, fully expanded
  collapseDepth: 6,
});
