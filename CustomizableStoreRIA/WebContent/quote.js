export const buildQuote = (q, role, isUnhandled) => {
    const quote = document.createElement('div');
    quote.className = 'quote-list-item';
    quote.id = `quote-list-item-${q.id}`;
    const imageContainer = document.createElement('div');
    imageContainer.className = 'quote-list-item-image';
    const image = document.createElement('img');
    image.src = `data:image/jpeg;base64,${q.product.image}`;
    image.width = 225;
    image.height = 150;
    imageContainer.appendChild(image);
    const infoContainer = document.createElement('div');
    infoContainer.className = 'quote-list-item-info';
    const date = document.createElement('div');
    date.className = 'quote-list-item-date';
    date.innerText = q.submissionDate;
    const name = document.createElement('h2');
    name.innerText = q.product.name;
    const optionListContainer = document.createElement('div');
    optionListContainer.className = 'quote-list-item-option-list';
    const ulEl = document.createElement('ul');
    q.options.forEach(o => {
        const option = document.createElement('li');
        const optionText = document.createElement('span');
        optionText.textContent = o.name;
        optionText.className = o.type.localeCompare('ON_SALE') === 0 && 'on-sale';
        option.appendChild(optionText);
        ulEl.appendChild(option);
    });
    optionListContainer.appendChild(ulEl);
    infoContainer.appendChild(date);
    infoContainer.appendChild(name);
    infoContainer.appendChild(optionListContainer);
    let price;
    if (role.localeCompare('ROLE_EMPLOYEE') === 0) {
        // show client name
        const clientName = document.createElement('h6');
        clientName.innerText = q.client;
        infoContainer.insertBefore(clientName, name);
    }
    if (isUnhandled) {
        price = document.createElement('button');
        price.type = 'button';
        price.innerHTML = 'Price quote';
        price.className = 'quote-list-item-price price-quote-button pointer';
        price.id = `price-quote-${q.id}`;
    } else {
        price = document.createElement('div');
        price.className = 'quote-list-item-price';
        price.innerText = q.price ? 'EUR ' + (Math.round(q.price * 100) / 100).toFixed(2) : 'ND';
    }
    infoContainer.appendChild(price);

    quote.appendChild(imageContainer);
    quote.appendChild(infoContainer);
    return quote;
}
