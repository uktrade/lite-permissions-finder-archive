@(googleAnalyticsId: String)
window.dataLayer = window.dataLayer || [];
function gtag(){dataLayer.push(arguments);}
gtag('js', new Date());
gtag('config', '@googleAnalyticsId', { 'anonymize_ip': true });